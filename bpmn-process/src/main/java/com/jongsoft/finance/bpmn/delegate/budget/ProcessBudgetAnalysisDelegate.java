package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * This delegate analyzes the transactions recorded with a specific budget type in the past 3 months
 * to determine if the total amount spent is in line with the expected amount spent. If it deviates
 * by more then 25% the analysis will report a deviation.
 *
 * <p>This will help identify budgets specified by the user that might need adjusting due to changed
 * spending patterns.
 */
@Slf4j
@Singleton
public class ProcessBudgetAnalysisDelegate implements JavaDelegate, JavaBean {

    private final TransactionProvider transactionProvider;
    private final FilterFactory filterFactory;
    private final SettingProvider settingProvider;

    ProcessBudgetAnalysisDelegate(
            TransactionProvider transactionProvider,
            FilterFactory filterFactory,
            SettingProvider settingProvider) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.settingProvider = settingProvider;
    }

    @Override
    public void execute(DelegateExecution execution) {
        var forExpense =
                execution
                        .<ObjectValue>getVariableLocalTyped("expense")
                        .getValue(Budget.Expense.class);
        var runningDate =
                LocalDate.parse(execution.<StringValue>getVariableLocalTyped("date").getValue());

        log.debug("Running budget '{}' analysis for {}", forExpense.getName(), runningDate);

        runningDate = runningDate.minusMonths(1);

        var deviation = 0d;
        var dateRange = DateUtils.forMonth(runningDate.getYear(), runningDate.getMonthValue());
        var budgetAnalysisMonths = settingProvider.getBudgetAnalysisMonths();
        var searchCommand =
                filterFactory
                        .transaction()
                        .expenses(Collections.List(new EntityRef(forExpense.getId())));

        for (int i = budgetAnalysisMonths; i > 0; i--) {
            var transactions = transactionProvider.lookup(searchCommand.range(dateRange));

            var spentInMonth =
                    transactions
                            .content()
                            .map(transaction -> transaction.computeAmount(transaction.computeTo()))
                            .sum()
                            .get();

            deviation += forExpense.computeBudget() - spentInMonth;
            dateRange = dateRange.previous();
        }

        var averageDeviation =
                BigDecimal.valueOf(deviation)
                        .divide(
                                BigDecimal.valueOf(budgetAnalysisMonths),
                                new MathContext(6, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
        if (Math.abs(averageDeviation) / forExpense.computeBudget()
                > settingProvider.getMaximumBudgetDeviation()) {
            execution.setVariableLocal("deviation", averageDeviation);
            execution.setVariableLocal("deviates", true);
        } else {
            execution.setVariableLocal("deviates", false);
            execution.setVariableLocal("deviation", 0);
        }
    }
}
