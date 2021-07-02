package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateExpenseHandler implements CommandHandler<CreateExpenseCommand> {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(CreateExpenseCommand command) {
        log.info("[{}] - Processing expense create event", command.name());

        var hql = """
                select b from BudgetJpa b
                where b.user.username = :username
                and b.from = :from""";

        var activeBudget = entityManager.<BudgetJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("from", command.start())
                .maybe()
                .get();

        var expenseJpa = ExpenseJpa.builder()
                .name(command.name())
                .user(activeBudget.getUser())
                .build();
        entityManager.persist(expenseJpa);

        var expensePeriodJpa = ExpensePeriodJpa.builder()
                .lowerBound(command.budget().subtract(new BigDecimal("0.01")))
                .upperBound(command.budget())
                .expense(expenseJpa)
                .budget(activeBudget)
                .build();
        entityManager.persist(expensePeriodJpa);

        // fix for when budget is created in same transaction (otherwise the list remains empty in hibernate session)
        activeBudget.getExpenses().add(expensePeriodJpa);
    }
}
