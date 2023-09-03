package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.ExpenseProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * This delegate allows for locating a specific budget in the system using one of the following methods:
 * <ol>
 *     <li><strong>name</strong>, the unique name of the budget</li>
 * </ol>
 *
 * The output of this delegate will be:
 * <ul>
 *     <li>{@code budget}, the {@link Budget} found</li>
 * </ul>
 */
@Slf4j
@Singleton
public class ProcessBudgetLookupDelegate implements JavaDelegate {

    private final FilterFactory filterFactory;
    private final ExpenseProvider expenseProvider;

    ProcessBudgetLookupDelegate(FilterFactory filterFactory, ExpenseProvider expenseProvider) {
        this.filterFactory = filterFactory;
        this.expenseProvider = expenseProvider;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.debug("{}: Processing budget lookup '{}'",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        var filter = filterFactory.expense()
                .name((String) execution.getVariableLocal("name"), true);
        var expense = expenseProvider.lookup(filter);
        if (expense.total() == 0) {
            throw new IllegalStateException("Budget cannot be found for name " + execution.getVariableLocal("name"));
        }

        execution.setVariable("budget", expense.content().head());
    }

}
