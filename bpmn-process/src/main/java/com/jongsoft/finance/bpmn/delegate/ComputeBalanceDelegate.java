package com.jongsoft.finance.bpmn.delegate;

import java.time.LocalDate;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.lang.API;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ComputeBalanceDelegate implements JavaDelegate {

    private final FilterFactory filterFactory;
    private final TransactionProvider transactionProvider;

    public ComputeBalanceDelegate(FilterFactory filterFactory, TransactionProvider transactionProvider) {
        this.filterFactory = filterFactory;
        this.transactionProvider = transactionProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        var requestBuilder = filterFactory.transaction();

        if (execution.hasVariableLocal("accountId")) {
            Long accountId = ((Number) execution.getVariableLocal("accountId")).longValue();
            requestBuilder.accounts(API.List(new EntityRef(accountId)));
        }

        if (execution.hasVariableLocal("date")) {
            String isoDate = execution.<StringValue>getVariableLocalTyped("date").getValue();
            requestBuilder.range(DateRange.of(LocalDate.of(1900, 1, 1), LocalDate.parse(isoDate)));
        } else {
            requestBuilder.range(DateRange.of(LocalDate.of(1900, 1, 1), LocalDate.of(2900, 1, 1)));
        }

        if (execution.hasVariableLocal("onlyIncome")) {
            boolean onlyIncome = execution.<BooleanValue>getVariableLocalTyped("onlyIncome").getValue();
            requestBuilder.onlyIncome(onlyIncome);
        }

        log.trace("{}: Computing the balance based upon {}", execution.getCurrentActivityName(), requestBuilder);

        var result = transactionProvider.balance(requestBuilder);
        execution.setVariableLocal("result", result.getOrSupply(() -> 0D));
    }

}