package com.jongsoft.finance.rest.process;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessVariableResourceTest {

    private ProcessVariableResource subject;

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private VariableInstanceQuery variableInstanceQuery;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createVariableInstanceQuery()).thenReturn(variableInstanceQuery);
        when(variableInstanceQuery.processInstanceIdIn(Mockito.anyString())).thenReturn(variableInstanceQuery);
        when(variableInstanceQuery.variableName(Mockito.anyString())).thenReturn(variableInstanceQuery);

        subject = new ProcessVariableResource(runtimeService);
    }

    @Test
    void variables() {
        subject.variables("procDefKey", "InstanceId");

        verify(variableInstanceQuery).processInstanceIdIn("InstanceId");
    }

    @Test
    void variable() {
        subject.variable("procDefKey", "InstanceId", "variable");

        verify(variableInstanceQuery).processInstanceIdIn("InstanceId");
        verify(variableInstanceQuery).variableName("variable");
    }

}
