package com.jongsoft.finance.jpa.budget;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

@ReadOnly
@Singleton
@RequiresJpa
@Named("budgetProvider")
public class BudgetProviderJpa implements BudgetProvider {

  private final Logger logger = getLogger(BudgetProviderJpa.class);

  private final AuthenticationFacade authenticationFacade;
  private final ReactiveEntityManager reactiveEntityManager;

  public BudgetProviderJpa(
      AuthenticationFacade authenticationFacade, ReactiveEntityManager reactiveEntityManager) {
    this.authenticationFacade = authenticationFacade;
    this.reactiveEntityManager = reactiveEntityManager;
  }

  @Override
  public Sequence<Budget> lookup() {
    logger.trace("Fetching all budgets for user.");

    return reactiveEntityManager
        .from(BudgetJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .orderBy("from", true)
        .stream()
        .map(this::convert)
        .collect(ReactiveEntityManager.sequenceCollector());
  }

  @Override
  public Optional<Budget> lookup(int year, int month) {
    logger.trace("Fetching budget for user in {}-{}.", year, month);
    var range = DateUtils.forMonth(year, month);

    return reactiveEntityManager
        .from(BudgetJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldLtOrEq("from", range.from())
        .fieldGtOrEqNullable("until", range.until())
        .singleResult()
        .map(this::convert);
  }

  @Override
  public Optional<Budget> first() {
    logger.trace("Fetching first budget for user.");

    return reactiveEntityManager
        .from(BudgetJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .orderBy("from", true)
        .limit(1)
        .singleResult()
        .map(this::convert);
  }

  private Budget convert(BudgetJpa source) {
    if (source == null) {
      return null;
    }

    var budget =
        Budget.builder()
            .id(source.getId())
            .start(source.getFrom())
            .end(source.getUntil())
            .expectedIncome(source.getExpectedIncome())
            .build();

    for (var expense : source.getExpenses()) {
      budget
      .new Expense(
          expense.getExpense().getId(),
          expense.getExpense().getName(),
          expense.getUpperBound().doubleValue());
    }

    return budget;
  }
}
