package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateBudgetHandler implements CommandHandler<CreateBudgetCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(CreateBudgetCommand command) {
        log.info("[{}] - Processing budget create event", command.budget().getStart());

        var budget = BudgetJpa.builder()
                .from(command.budget().getStart())
                .expectedIncome(command.budget().getExpectedIncome())
                .expenses(new HashSet<>())
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
                .build();

        entityManager.persist(budget);

        Control.Option(command.budget().getExpenses())
                .ifPresent(expenses -> budget.getExpenses().addAll(createExpenses(budget, expenses)));
    }

    private Collection<ExpensePeriodJpa> createExpenses(BudgetJpa budget, Sequence<Budget.Expense> expenses) {
        return expenses.map(expense ->
                ExpensePeriodJpa.builder()
                        .budget(budget)
                        .expense(entityManager.get(ExpenseJpa.class, Collections.Map("id", expense.getId())))
                        .lowerBound(BigDecimal.valueOf(expense.getLowerBound()).subtract(new BigDecimal("0.001")))
                        .upperBound(BigDecimal.valueOf(expense.getUpperBound()))
                        .build())
                .map(this::persist)
                .toJava();
    }

    private <U extends EntityJpa> U persist(U entity) {
        entityManager.persist(entity);
        return entity;
    }
}
