package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.finance.providers.BudgetProvider;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
@Singleton
public class ProcessBudgetMonthSelect implements JavaDelegate, JavaBean {

    private final BudgetProvider budgetProvider;

    @Inject
    public ProcessBudgetMonthSelect(BudgetProvider budgetProvider) {
        this.budgetProvider = budgetProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var year = Integer.parseInt(execution.getVariableLocal("year").toString());
        var month = Integer.parseInt(execution.getVariableLocal("month").toString());

        log.debug("Processing budget month select for {}-{}", year, month);

        budgetProvider
                .lookup(year, month)
                .ifPresent(budget ->
                        execution.setVariable("expenses", budget.getExpenses().toJava()))
                .elseThrow(() -> new IllegalStateException(
                        "Budget cannot be found for year " + year + " and month " + month));
    }
}
