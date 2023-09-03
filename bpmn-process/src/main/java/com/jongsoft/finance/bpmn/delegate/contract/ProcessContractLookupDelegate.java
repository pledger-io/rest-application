package com.jongsoft.finance.bpmn.delegate.contract;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.providers.ContractProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Singleton
public class ProcessContractLookupDelegate implements JavaDelegate {

    private final ContractProvider contractProvider;

    ProcessContractLookupDelegate(ContractProvider contractProvider) {
        this.contractProvider = contractProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing contract lookup '{}'",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        final Contract contract;
        if (execution.hasVariableLocal("name")) {
            var name = (String) execution.getVariableLocal("name");
            contract = contractProvider.lookup(name)
                    .defaultIfEmpty(Contract.builder().name(name).build())
                    .block(Duration.of(500, ChronoUnit.MILLIS));
        } else {
            contract = contractProvider.lookup((Long) execution.getVariableLocal("id"))
                    .getOrSupply(() -> null);
        }

        execution.setVariable("contract", contract);
    }

}
