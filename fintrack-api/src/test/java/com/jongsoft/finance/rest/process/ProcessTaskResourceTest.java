package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.TestSetup;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

@DisplayName("Process task resource")
class ProcessTaskResourceTest extends TestSetup {

    private final TaskService taskService = Mockito.mock(TaskService.class);

    @Replaces
    @MockBean
    TaskService taskService() {
        return taskService;
    }

    @Replaces
    @MockBean
    ProcessEngine processEngine() {
        var engine = Mockito.mock(ProcessEngine.class);
        Mockito.when(engine.getTaskService()).thenReturn(taskService);
        Mockito.when(engine.getHistoryService()).thenReturn(Mockito.mock(HistoryService.class));
        Mockito.when(engine.getRuntimeService()).thenReturn(Mockito.mock(RuntimeService.class));
        return engine;
    }

    @Test
    @DisplayName("should return tasks")
    void tasks(RequestSpecification spec) {
        var taskMock = Mockito.mock(TaskQuery.class);

        Mockito.when(taskService.createTaskQuery()).thenReturn(taskMock);
        Mockito.when(taskMock.processInstanceId("1")).thenReturn(taskMock);
        Mockito.when(taskMock.processDefinitionKey("reconcileAccount")).thenReturn(taskMock);
        Mockito.when(taskMock.initializeFormKeys()).thenReturn(taskMock);
        Mockito.when(taskMock.list()).thenReturn(List.of());

        // @formatter:off
        spec.when()
                .get("/api/runtime-process/{task}/1/{id}/tasks", "reconcileAccount", "1")
            .then()
                .statusCode(200);
        // @formatter:on
    }

    @Test
    @DisplayName("should complete task")
    void complete(RequestSpecification spec) {

        // @formatter:off
        spec.when()
                .delete("/api/runtime-process/reconcileAccount/1/1/tasks/{task}", "L")
            .then()
                .statusCode(200);
        // @formatter:on

        Mockito.verify(taskService).complete("L");
    }

}
