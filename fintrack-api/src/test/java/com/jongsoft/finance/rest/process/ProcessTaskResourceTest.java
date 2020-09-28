package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessTaskResponse;
import io.reactivex.subscribers.TestSubscriber;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

class ProcessTaskResourceTest {

    private ProcessTaskResource subject;

    @Mock
    private TaskService taskService;
    @Mock
    private ProcessEngine processEngine;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(processEngine.getTaskService()).thenReturn(taskService);
        subject = new ProcessTaskResource(processEngine);
    }

    @Test
    void tasks() {
        TaskQuery taskMock = Mockito.mock(TaskQuery.class);

        Mockito.when(taskService.createTaskQuery()).thenReturn(taskMock);
        Mockito.when(taskMock.processInstanceId("1")).thenReturn(taskMock);
        Mockito.when(taskMock.processDefinitionKey("reconcileAccount")).thenReturn(taskMock);
        Mockito.when(taskMock.initializeFormKeys()).thenReturn(taskMock);
        Mockito.when(taskMock.list()).thenReturn(List.of());

        TestSubscriber<ProcessTaskResponse> subscriber = new TestSubscriber<>();
        subject.tasks("reconcileAccount", "1")
                .subscribe(subscriber);

        subscriber.assertComplete();
        subscriber.assertValueCount(0);
    }

    @Test
    void complete() {
        subject.complete("L");

        Mockito.verify(taskService).complete("L");
    }

}