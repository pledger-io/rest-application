package com.jongsoft.finance.rest.process;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(processEngine.getHistoryService()).thenReturn(historyService);
        when(historyService.createHistoricVariableInstanceQuery()).thenReturn(variableInstanceQuery);
        when(variableInstanceQuery.processDefinitionKey(Mockito.anyString())).thenReturn(variableInstanceQuery);
        when(variableInstanceQuery.processInstanceId(Mockito.anyString())).thenReturn(variableInstanceQuery);
        when(variableInstanceQuery.variableName(Mockito.anyString())).thenReturn(variableInstanceQuery);

        subject = new ProcessVariableResource(historyService);
    }

    @Test
    void variables() {
        StepVerifier.create(subject.variables("procDefKey", "InstanceId"))
                .verifyComplete();

        verify(variableInstanceQuery).processDefinitionKey("procDefKey");
        verify(variableInstanceQuery).processInstanceId("InstanceId");
    }

    @Test
    void variable() {
        StepVerifier.create(subject.variable("procDefKey", "InstanceId", "variable"))
                .verifyComplete();

        verify(variableInstanceQuery).processDefinitionKey("procDefKey");
        verify(variableInstanceQuery).processInstanceId("InstanceId");
        verify(variableInstanceQuery).variableName("variable");
    }

}
