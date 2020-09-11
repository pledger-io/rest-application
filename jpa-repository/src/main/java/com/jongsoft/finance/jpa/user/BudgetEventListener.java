package com.jongsoft.finance.jpa.user;

import java.util.HashSet;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.events.BudgetClosedEvent;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.domain.user.events.BudgetExpenseCreatedEvent;
import com.jongsoft.finance.jpa.core.RepositoryJpa;
import com.jongsoft.finance.jpa.user.entity.BudgetJpa;
import com.jongsoft.finance.jpa.user.entity.ExpenseJpa;
import com.jongsoft.finance.jpa.user.entity.ExpensePeriodJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

@Singleton
@Transactional
public class BudgetEventListener extends RepositoryJpa {

    private final AuthenticationFacade authenticationFacade;
    private final EntityManager entityManager;
    private final Logger logger;

    public BudgetEventListener(AuthenticationFacade authenticationFacade, EntityManager entityManager) {
        this.authenticationFacade = authenticationFacade;
        this.entityManager = entityManager;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @BusinessEventListener
    public void handleBudgetCreatedEvent(BudgetCreatedEvent event) {
        logger.trace("[{}] - Processing budget create event", event.getBudget().getStart());

        var budget = BudgetJpa.builder()
                .from(event.getBudget().getStart())
                .expectedIncome(event.getBudget().getExpectedIncome())
                .expenses(new HashSet<>())
                .user(lookup())
                .build();

        entityManager.persist(budget);

        if (event.getBudget().getExpenses() != null && !event.getBudget().getExpenses().isEmpty()) {
            event.getBudget().getExpenses()
                    .stream()
                    .map(e -> {
                        var expenseJpa = entityManager.find(ExpenseJpa.class, e.getId());

                        return ExpensePeriodJpa.builder()
                                .lowerBound(e.getLowerBound())
                                .upperBound(e.getUpperBound())
                                .expense(expenseJpa)
                                .budget(budget)
                                .build();
                    })
                    .forEach(expense -> {
                        entityManager.persist(expense);
                        budget.getExpenses().add(expense);
                    });
        }
    }

    @BusinessEventListener
    public void handleBudgetClosedEvent(BudgetClosedEvent event) {
        logger.trace("[{}] - Processing budget closing event", event.getId());

        var hql = """
                update BudgetJpa 
                set until = :end
                where id = :id""";

        var query = entityManager.createQuery(hql);
        query.setParameter("id", event.getId());
        query.setParameter("end", event.getEndDate());
        query.executeUpdate();
    }

    @BusinessEventListener
    public void handleExpenseCreatedEvent(BudgetExpenseCreatedEvent event) {
        logger.trace("[{}] - Processing expense create event", event.getName());

        var hql = """
                select b from BudgetJpa b 
                where b.user.username = :username 
                and b.from = :from""";

        var query = entityManager.createQuery(hql);
        query.setParameter("username", authenticationFacade.authenticated());
        query.setParameter("from", event.getStart());

        BudgetJpa activeBudget = singleValue(query);
        var expenseJpa = ExpenseJpa.builder()
                .name(event.getName())
                .user(lookup())
                .build();
        entityManager.persist(expenseJpa);

        var expensePeriodJpa = ExpensePeriodJpa.builder()
                .lowerBound(event.getLowerBound())
                .upperBound(event.getUpperBound())
                .expense(expenseJpa)
                .budget(activeBudget)
                .build();
        entityManager.persist(expensePeriodJpa);

        // fix for when budget is created in same transaction (otherwise the list remains empty in hibernate session)
        activeBudget.getExpenses().add(expensePeriodJpa);
    }

    private UserAccountJpa lookup() {
        var query = entityManager.createQuery("select a from UserAccountJpa a where a.username = :username");
        query.setParameter("username", authenticationFacade.authenticated());
        return singleValue(query);
    }

}
