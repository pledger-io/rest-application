package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.BudgetJson;
import com.jongsoft.lang.Control;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

/**
 * This delegate will create process a serialize {@link BudgetJson} into a new budget period in the system. It will
 * either create the first budget or close the existing and create a new one.
 * <p>
 * This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>budget, a JSON serialized {@link BudgetJson} to be created in the system</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessBudgetCreateDelegate implements JavaDelegate, JavaBean {

    private final BudgetProvider budgetProvider;
    private final ProcessMapper mapper;

    ProcessBudgetCreateDelegate(
            BudgetProvider budgetProvider,
            ProcessMapper mapper) {
        this.budgetProvider = budgetProvider;
        this.mapper = mapper;
    }

    @Override
    public void execute(DelegateExecution execution) {
        var budgetJson = mapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("budget").getValue(),
                BudgetJson.class);

        log.debug("{}: Processing budget creation from json for period '{}'",
                execution.getCurrentActivityName(),
                budgetJson.getStart());

        var start = budgetJson.getStart().withDayOfMonth(1);
        var year = budgetJson.getStart().getYear();
        var month = budgetJson.getStart().getMonthValue();

        // create or update the budget period
        var oldBudget = budgetProvider.lookup(year, month);
        if (oldBudget.isPresent()) {
            log.debug("{}: Budget period already exists for period '{}' with start '{}'",
                    execution.getCurrentActivityName(),
                    start,
                    oldBudget.get().getStart());
            // close the existing budget
            EventBus.getBus()
                    .send(new CloseBudgetCommand(oldBudget.get().getId(), start));
            // create new budget
            EventBus.getBus()
                    .send(new CreateBudgetCommand(Budget.builder()
                            .start(start)
                            .expectedIncome(budgetJson.getExpectedIncome())
                            .expenses(oldBudget.get().getExpenses())
                            .build()));
        } else {
            log.debug("{}: Creating new budget period for period '{}'",
                    execution.getCurrentActivityName(),
                    start);
            EventBus.getBus().send(new CreateBudgetCommand(
                    Budget.builder()
                            .start(start)
                            .expectedIncome(budgetJson.getExpectedIncome())
                            .build()));
        }

        log.trace("{}: Budget period updated for period '{}'",
                execution.getCurrentActivityName(),
                budgetJson.getStart());

        var budget = budgetProvider.lookup(year, month)
                .getOrThrow(() -> new IllegalStateException("Budget period not found for period " + start));

        budgetJson.getExpenses()
                // update or create the expenses
                .forEach(e -> Control.Option(budget.determineExpense(e.getName()))
                        .ifPresent(currentExpense -> currentExpense.updateExpense(e.getUpperBound()))
                        .elseRun(() -> budget.createExpense(e.getName(), e.getLowerBound(), e.getUpperBound())));
    }

}
