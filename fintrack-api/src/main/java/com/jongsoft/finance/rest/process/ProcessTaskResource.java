package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessTaskResponse;
import com.jongsoft.lang.Collections;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;

import javax.inject.Inject;

@Tag(name = "Process Engine")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Controller("/api/runtime-process/{processDefinitionKey}/{businessKey}/{instanceId}/tasks")
public class ProcessTaskResource {

    private final TaskService taskService;

    @Get
    @Operation(
            summary = "List Tasks",
            description = "List all available tasks for the provided process",
            operationId = "getTasks"
    )
    public Flowable<ProcessTaskResponse> tasks(@PathVariable String processDefinitionKey, @PathVariable String instanceId) {
        var tasks = Collections.List(taskService.createTaskQuery()
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
