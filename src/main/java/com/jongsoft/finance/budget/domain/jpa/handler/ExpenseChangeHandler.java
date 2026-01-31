package com.jongsoft.finance.budget.domain.jpa.handler;

import com.jongsoft.finance.budget.domain.commands.CreateExpenseCommand;
import com.jongsoft.finance.budget.domain.commands.UpdateExpenseCommand;
import com.jongsoft.finance.budget.domain.jpa.entity.BudgetJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpenseJpa;
import com.jongsoft.finance.budget.domain.jpa.entity.ExpensePeriodJpa;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.jpa.query.ReactiveEntityManager;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;

import java.math.BigDecimal;

@Singleton
@Transactional
class ExpenseChangeHandler {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(ExpenseChangeHandler.class);
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    ExpenseChangeHandler(
            ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @EventListener
    public void handleExpense(CreateExpenseCommand command) {
        log.info("[{}] - Processing expense create event", command.name());

        var activeBudget = entityManager
                .from(BudgetJpa.class)
                .fieldEq("user.username", authenticationFacade.authenticated())
                .fieldEq("from", command.start())
                .singleResult()
                .get();

        var expenseJpa = new ExpenseJpa(command.name(), activeBudget.getUser());
        entityManager.persist(expenseJpa);

        var expensePeriodJpa = new ExpensePeriodJpa(
                command.budget().subtract(new BigDecimal("0.01")),
                command.budget(),
                expenseJpa,
                activeBudget);
        entityManager.persist(expensePeriodJpa);

        // fix for when budget is created in same transaction (otherwise the list remains empty in
        // hibernate session)
        activeBudget.getExpenses().add(expensePeriodJpa);
    }

    @EventListener
    public void handleUpdate(UpdateExpenseCommand command) {
        var existing = entityManager
                .from(ExpensePeriodJpa.class)
                .fieldEq("expense.id", command.id())
                .fieldEq("budget.user.username", authenticationFacade.authenticated())
                .fieldNull("budget.until")
                .singleResult()
                .getOrThrow(() -> new RuntimeException("Unable to find expense"));

        log.info("[{}] - Processing expense update event", existing.getId());

        existing.setLowerBound(command.amount().subtract(BigDecimal.valueOf(.01)));
        existing.setUpperBound(command.amount());
        entityManager.persist(existing);
    }
}
