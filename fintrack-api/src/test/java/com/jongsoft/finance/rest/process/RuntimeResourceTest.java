package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessResponse;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.reactivex.subscribers.TestSubscriber;
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

        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito.when(processEngine.getHistoryService()).thenReturn(historyService);
        Mockito.when(authenticationFacade.authenticated()).thenReturn("test-user");

        subject = new RuntimeResource(processEngine, authenticationFacade);
    }

    @Test
    void startProcess() {
        var mockInstance = Mockito.mock(ProcessInstance.class);
        var historyMock = Mockito.mock(HistoricProcessInstance.class);
        var instanceBuilder = Mockito.mock(ProcessInstantiationBuilder.class);
        var historyBuilder = Mockito.mock(HistoricProcessInstanceQuery.class);

        Mockito.when(runtimeService.createProcessInstanceByKey("testProcess")).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.execute()).thenReturn(mockInstance);
        Mockito.when(mockInstance.getProcessInstanceId()).thenReturn("MockProcessInstance");
        Mockito.when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historyBuilder);
        Mockito.when(historyBuilder.processInstanceId("MockProcessInstance")).thenReturn(historyBuilder);
        Mockito.when(historyBuilder.singleResult()).thenReturn(historyMock);

        subject.startProcess("testProcess", Map.of("businessKey", "sample-key")).blockingGet();

        Mockito.verify(runtimeService).createProcessInstanceByKey("testProcess");
        Mockito.verify(instanceBuilder).businessKey("sample-key");
    }

    @Test
    void history() {
        final HistoricProcessInstanceQuery instanceBuilder = Mockito.mock(HistoricProcessInstanceQuery.class);

        Mockito.when(historyService.createHistoricProcessInstanceQuery()).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.processDefinitionKey("testProcess")).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.variableValueEquals("username", "test-user")).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.orderByProcessInstanceStartTime()).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.desc()).thenReturn(instanceBuilder);
        Mockito.when(instanceBuilder.list()).thenReturn(List.of());

        TestSubscriber<ProcessResponse> subscriber = new TestSubscriber<>();
        subject.history("testProcess")
                .subscribe(subscriber);

        subscriber.assertComplete();

        Mockito.verify(historyService).createHistoricProcessInstanceQuery();
        Mockito.verify(instanceBuilder).processDefinitionKey("testProcess");
    }

    @Test
    void deleteProcess() {
        subject.deleteProcess("procId", "BusKey", "InstanceId");

        Mockito.verify(runtimeService).deleteProcessInstance("InstanceId", "User termination");
    }

    @Test
    void cleanHistory() {
        subject.cleanHistory();

        Mockito.verify(historyService).cleanUpHistoryAsync(true);
    }
}