package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.BudgetJson;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This delegate will create process a serialize {@link BudgetJson} into a new budget period in the system. It will
 * either create the first budget or close the existing and create a new one.
 * <p>
 *      This delegate expects the following variables to be present:
 * </p>
 * <ul>
 *     <li>budget, a JSON serialized {@link BudgetJson} to be created in the system</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessBudgetCreateDelegate implements JavaDelegate {

    private final CurrentUserProvider currentUserProvider;
    private final BudgetProvider budgetProvider;

    @Inject
    public ProcessBudgetCreateDelegate(
            CurrentUserProvider currentUserProvider,
            BudgetProvider budgetProvider) {
        this.currentUserProvider = currentUserProvider;
        this.budgetProvider = budgetProvider;
    }

    @Override
    public void execute(DelegateExecution execution) {
        var userAccount = currentUserProvider.currentUser();
        var budgetJson = ProcessMapper.readSafe(
                execution.<StringValue>getVariableLocalTyped("budget").getValue(),
                BudgetJson.class);

        log.debug("{}: Processing budget creation from json for period '{}'",
                execution.getCurrentActivityName(),
                budgetJson.getStart());

        var year = budgetJson.getStart().getYear();
        var month = budgetJson.getStart().getMonthValue();

        var budget = budgetProvider.lookup(year, month)
                .map(b -> b.indexBudget(budgetJson.getStart(), budgetJson.getExpectedIncome()))
                .onErrorReturn(e -> userAccount.createBudget(budgetJson.getStart(), budgetJson.getExpectedIncome()))
                .blockingGet();

        budgetJson.getExpenses().stream()
                .filter(e -> budget.determineExpense(e.getName()) == null)
                .forEach(e -> budget.createExpense(e.getName(), e.getLowerBound(), e.getUpperBound()));
    }

}
