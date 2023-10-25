package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.security.AuthenticationFacade;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeResourceTest {

    private RuntimeResource subject;

    @Mock
    private ProcessEngine processEngine;
    @Mock
    private HistoryService historyService;
    @Mock
    private RuntimeService runtimeService;
    @Mock
    private AuthenticationFacade authenticationFacade;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        when(processEngine.getHistoryService()).thenReturn(historyService);
        when(authenticationFacade.authenticated()).thenReturn("test-user");

        subject = new RuntimeResource(historyService, runtimeService, authenticationFacade);
    }

    @Test
    void startProcess() {
        var mockInstance = Mockito.mock(ProcessInstance.class);
        var historyMock = Mockito.mock(HistoricProcessInstance.class);
        var instanceBuilder = Mockito.mock(ProcessInstantiationBuilder.class);
        var historyBuilder = Mockito.mock(HistoricProcessInstanceQuery.class);

        when(runtimeService.createProcessInstanceByKey("testProcess")).thenReturn(instanceBuilder);
        when(instanceBuilder.execute()).thenReturn(mockInstance);
        when(mockInstance.getProcessInstanceId()).thenReturn("MockProcessInstance");
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historyBuilder);
        when(historyBuilder.processInstanceId("MockProcessInstance")).thenReturn(historyBuilder);
        when(historyBuilder.singleResult()).thenReturn(historyMock);

        when(historyMock.getId()).thenReturn("MockProcessInstance");
        when(historyMock.getBusinessKey()).thenReturn("sample-key");

        Assertions.assertThat(subject.startProcess("testProcess", Map.of("businessKey", "sample-key")))
                .satisfies(instance -> {
                    Assertions.assertThat(instance.getId()).isEqualTo("MockProcessInstance");
                    Assertions.assertThat(instance.getBusinessKey()).isEqualTo("sample-key");
                });

        verify(runtimeService).createProcessInstanceByKey("testProcess");
        verify(instanceBuilder).businessKey("sample-key");
    }

    @Test
    void history() {
        final HistoricProcessInstanceQuery instanceBuilder = Mockito.mock(HistoricProcessInstanceQuery.class);

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(instanceBuilder);
        when(instanceBuilder.processDefinitionKey("testProcess")).thenReturn(instanceBuilder);
        when(instanceBuilder.variableValueEquals("username", "test-user")).thenReturn(instanceBuilder);
        when(instanceBuilder.orderByProcessInstanceStartTime()).thenReturn(instanceBuilder);
        when(instanceBuilder.desc()).thenReturn(instanceBuilder);
        when(instanceBuilder.list()).thenReturn(List.of());

        subject.history("testProcess");

        verify(historyService).createHistoricProcessInstanceQuery();
        verify(instanceBuilder).processDefinitionKey("testProcess");
    }

    @Test
    void deleteProcess() {
        subject.deleteProcess("procId", "BusKey", "InstanceId");

        verify(runtimeService).deleteProcessInstance("InstanceId", "User termination");
    }

    @Test
    void cleanHistory() {
        subject.cleanHistory();

        verify(historyService).cleanUpHistoryAsync(true);
    }
}
