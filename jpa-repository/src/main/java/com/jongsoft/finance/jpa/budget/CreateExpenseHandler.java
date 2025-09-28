package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateExpenseHandler implements CommandHandler<CreateExpenseCommand> {

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  @Inject
  public CreateExpenseHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(CreateExpenseCommand command) {
    log.info("[{}] - Processing expense create event", command.name());

    var activeBudget =
        entityManager
            .from(BudgetJpa.class)
            .fieldEq("user.username", authenticationFacade.authenticated())
            .fieldEq("from", command.start())
            .singleResult()
            .get();

    var expenseJpa = ExpenseJpa.builder().name(command.name()).user(activeBudget.getUser()).build();
    entityManager.persist(expenseJpa);

    var expensePeriodJpa =
        ExpensePeriodJpa.builder()
            .lowerBound(command.budget().subtract(new BigDecimal("0.01")))
            .upperBound(command.budget())
            .expense(expenseJpa)
            .budget(activeBudget)
            .build();
    entityManager.persist(expensePeriodJpa);

    // fix for when budget is created in same transaction (otherwise the list remains empty in
    // hibernate session)
    activeBudget.getExpenses().add(expensePeriodJpa);
  }
}
