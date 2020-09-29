package com.jongsoft.finance.rest.process;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ProcessVariableResourceTest {

    private ProcessVariableResource subject;

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private HistoryService historyService;

    @Mock
    private HistoricVariableInstanceQuery variableInstanceQuery;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(processEngine.getHistoryService()).thenReturn(historyService);
        Mockito.when(historyService.createHistoricVariableInstanceQuery()).thenReturn(variableInstanceQuery);
        Mockito.when(variableInstanceQuery.processDefinitionKey(Mockito.anyString())).thenReturn(variableInstanceQuery);
        Mockito.when(variableInstanceQuery.processInstanceId(Mockito.anyString())).thenReturn(variableInstanceQuery);
        Mockito.when(variableInstanceQuery.variableName(Mockito.anyString())).thenReturn(variableInstanceQuery);

        subject = new ProcessVariableResource(processEngine);
    }

    @Test
    void variables() {
        var checker = subject.variables("procDefKey", "InstanceId")
                .test();

        checker.assertComplete();

        Mockito.verify(variableInstanceQuery).processDefinitionKey("procDefKey");
        Mockito.verify(variableInstanceQuery).processInstanceId("InstanceId");
    }

    @Test
    void variable() {
        var checker = subject.variable("procDefKey", "InstanceId", "variable")
                .test();

        checker.assertComplete();

        Mockito.verify(variableInstanceQuery).processDefinitionKey("procDefKey");
        Mockito.verify(variableInstanceQuery).processInstanceId("InstanceId");
        Mockito.verify(variableInstanceQuery).variableName("variable");
    }

}