package com.jongsoft.finance.bpmn.delegate.contract;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.account.ContractProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProcessContractLookupDelegate implements JavaDelegate {

    private final ContractProvider contractProvider;

    public ProcessContractLookupDelegate(ContractProvider contractProvider) {
        this.contractProvider = contractProvider;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("{}: Processing contract lookup '{}'",
                execution.getCurrentActivityName(),
                execution.getVariableLocal("name"));

        final Contract contract;
        if (execution.hasVariableLocal("name")) {
            contract = contractProvider.lookup((String) execution.getVariableLocal("name"))
                    .getOrSupply(() -> null);
        } else {
            contract = contractProvider.lookup((Long) execution.getVariableLocal("id"))
                    .getOrSupply(() -> null);
        }

        execution.setVariable("contract", contract);
    }

}
