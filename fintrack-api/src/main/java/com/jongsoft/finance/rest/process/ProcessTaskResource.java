package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessTaskResponse;
import com.jongsoft.lang.API;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;

@Tag(name = "Process Engine")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/runtime-process/{processDefinitionKey}/{businessKey}/{instanceId}/tasks")
public class ProcessTaskResource {

    private final TaskService taskService;

    public ProcessTaskResource(ProcessEngine processEngine) {
        this.taskService = processEngine.getTaskService();
    }

    @Get
    @Operation(
            summary = "List Tasks",
            description = "List all available tasks for the provided process",
            operationId = "getTasks"
    )
    public Flowable<ProcessTaskResponse> tasks(@PathVariable String processDefinitionKey, @PathVariable String instanceId) {
        var tasks = API.List(taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceId(instanceId)
                .initializeFormKeys()
                .list())
                .map(ProcessTaskResponse::new);

        return Flowable.fromIterable(tasks);
    }

    @Delete("/{taskId}")
    @Operation(
            summary = "Complete Task",
            description = "Completes the given task",
            operationId = "deleteTask"
    )
    public void complete(@PathVariable String taskId) {
        taskService.complete(taskId);
    }

}
