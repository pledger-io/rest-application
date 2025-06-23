package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Transactional
public class CreateBudgetHandler implements CommandHandler<CreateBudgetCommand> {

  private final ReactiveEntityManager entityManager;

  @Inject
  public CreateBudgetHandler(ReactiveEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @BusinessEventListener
  public void handle(CreateBudgetCommand command) {
    log.info("[{}] - Processing budget create event", command.budget().getStart());

    var budget = BudgetJpa.builder()
        .from(command.budget().getStart())
        .expectedIncome(command.budget().getExpectedIncome())
        .expenses(new HashSet<>())
        .user(entityManager.currentUser())
        .build();

    entityManager.persist(budget);

    Control.Option(command.budget().getExpenses())
        .ifPresent(expenses -> budget.getExpenses().addAll(createExpenses(budget, expenses)));
  }

  private Collection<ExpensePeriodJpa> createExpenses(
      BudgetJpa budget, Sequence<Budget.Expense> expenses) {
    log.debug("Creating {} expenses for budget period {}", expenses.size(), budget.getFrom());
    return expenses
        .map(expense -> ExpensePeriodJpa.builder()
            .budget(budget)
            .expense(entityManager.getById(ExpenseJpa.class, expense.getId()))
            .lowerBound(
                BigDecimal.valueOf(expense.getLowerBound()).subtract(new BigDecimal("0.001")))
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
