package com.jongsoft.finance.bpmn.delegate.contract;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.lang.Control;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

class ProcessContractLookupDelegateTest {

    private ContractProvider contractProvider;
    private DelegateExecution execution;

    private ProcessContractLookupDelegate subject;

    @BeforeEach
    void setup() {
        contractProvider = Mockito.mock(ContractProvider.class);
        execution = Mockito.mock(DelegateExecution.class);

        subject = new ProcessContractLookupDelegate(contractProvider);
    }

    @Test
    void execute_byName() throws Exception {
        Contract contract = Contract.builder().build();

        Mockito.when(contractProvider.lookup("Contract 1")).thenReturn(Mono.just(contract));
        Mockito.when(execution.hasVariableLocal("name")).thenReturn(true);
        Mockito.when(execution.getVariableLocal("name")).thenReturn("Contract 1");

        subject.execute(execution);

        Mockito.verify(execution).setVariable("contract", contract);
    }

    @Test
    void execute_byId() throws Exception {
        Contract contract = Contract.builder().build();

        Mockito.when(contractProvider.lookup(2L)).thenReturn(Control.Option(contract));
        Mockito.when(execution.hasVariableLocal("name")).thenReturn(false);
        Mockito.when(execution.getVariableLocal("id")).thenReturn(2L);

        subject.execute(execution);

        Mockito.verify(execution).setVariable("contract", contract);
    }

}
