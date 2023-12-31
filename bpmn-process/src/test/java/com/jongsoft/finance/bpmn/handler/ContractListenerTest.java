package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

class ContractListenerTest {

    private ProcessEngine processEngine;

    private ProcessInstanceQuery queryMock;
    private JobQuery jobQuery;
    private ProcessInstantiationBuilder processInstantiationBuilder;
    private AuthenticationFacade authenticationFacade;

    @BeforeEach
    void setup() {
        authenticationFacade = Mockito.mock(AuthenticationFacade.class);
        processEngine = Mockito.mock(ProcessEngine.class);
        queryMock = Mockito.mock(ProcessInstanceQuery.class, arg -> queryMock);
        jobQuery = Mockito.mock(JobQuery.class, arguments -> jobQuery);
        processInstantiationBuilder = Mockito.mock(ProcessInstantiationBuilder.class);

        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        ManagementService managementService = Mockito.mock(ManagementService.class);
        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito.when(processEngine.getManagementService()).thenReturn(managementService);

        Mockito.when(managementService.createJobQuery()).thenReturn(jobQuery);
        Mockito.when(runtimeService.createProcessInstanceQuery()).thenReturn(queryMock);
        Mockito.when(runtimeService.createProcessInstanceByKey("ContractEndWarning")).thenReturn(processInstantiationBuilder);

        Mockito.doReturn(processInstantiationBuilder).when(processInstantiationBuilder).businessKey(Mockito.anyString());
        Mockito.when(processInstantiationBuilder.setVariable(Mockito.anyString(), Mockito.any())).thenReturn(processInstantiationBuilder);

        Mockito.when(authenticationFacade.authenticated()).thenReturn("test-user");
    }

    @Test
    void handleContractEnd() {
        var job = Mockito.mock(Job.class);
        var instance = Mockito.mock(ProcessInstance.class);

        Mockito.doReturn(instance).when(queryMock).singleResult();
        Mockito.doReturn(job).when(jobQuery).singleResult();
        Mockito.when(instance.getProcessInstanceId()).thenReturn("test_instance_1");

        new ChangeContractHandler(processEngine).handle(
                new ChangeContractCommand(
                        1L,
                        "",
                        "",
                        LocalDate.of(2019, 1, 31),
                        LocalDate.of(2030, 1, 1)));

        Mockito.verify(processEngine).getRuntimeService();
        Mockito.verify(queryMock).processDefinitionKey("ContractEndWarning");
        Mockito.verify(queryMock).processInstanceBusinessKey("contract_term_" + 1);

        Mockito.verify(processEngine, Mockito.times(2)).getManagementService();
    }

    @Test
    void handleShouldWarn() {
        new WarnBeforeExpiryHandler(processEngine, authenticationFacade)
                .handle(new WarnBeforeExpiryCommand(1L, LocalDate.now().plusMonths(2)));

        // In some rare cases plus 1 month does not equal plus 2 months minus 1 month
        var endDate = DateUtils.toDate(LocalDate.now().plusMonths(2).minusMonths(1));

        Mockito.verify(processEngine).getRuntimeService();
        Mockito.verify(processInstantiationBuilder).businessKey("contract_term_" + 1);
        Mockito.verify(processInstantiationBuilder).setVariable("warnAt", endDate);
        Mockito.verify(processInstantiationBuilder).setVariable("username", "test-user");
        Mockito.verify(processInstantiationBuilder).execute();
    }

}
