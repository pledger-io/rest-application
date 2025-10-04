package com.jongsoft.finance.rest.process;

import static com.jongsoft.finance.rest.ApiConstants.TAG_AUTOMATION_PROCESSES;

import com.jongsoft.finance.rest.model.ProcessTaskResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.lang.Collections;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.variable.Variables;

import java.util.HashMap;
import java.util.List;

@Tag(name = TAG_AUTOMATION_PROCESSES)
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
@Controller("/api/runtime-process/{processDefinitionKey}/{businessKey}/{instanceId}/tasks")
public class ProcessTaskResource {

    private final TaskService taskService;

    public ProcessTaskResource(TaskService taskService) {
        this.taskService = taskService;
    }

    @Get
    @Operation(
            summary = "List Tasks",
            description = "List all available tasks for the provided process",
            operationId = "getTasks")
    public List<ProcessTaskResponse> tasks(
            @PathVariable String processDefinitionKey, @PathVariable String instanceId) {
        return Collections.List(taskService
                        .createTaskQuery()
                        .processDefinitionKey(processDefinitionKey)
                        .processInstanceId(instanceId)
                        .initializeFormKeys()
                        .list())
                .map(ProcessTaskResponse::new)
                .toJava();
    }

    @Get("/{taskId}/variables")
    @Operation(
            summary = "Get Task",
            description = "Get the details of the given task.",
            operationId = "getTask")
    public synchronized VariableMap variables(
            @PathVariable String taskId, @Nullable @QueryValue String variable) {
        var variableMap = new VariableMap();
        if (variable != null) {
            variableMap.put(variable, taskService.getVariable(taskId, variable));
        } else {
            taskService.getVariables(taskId).forEach(variableMap::put);
        }

        return variableMap;
    }

    @Post("/{taskId}/complete")
    @Operation(
            summary = "Complete Task",
            description = "Completes the given task with the provided data.",
            operationId = "completeTask")
    public void complete(@PathVariable String taskId, @Body VariableMap variables) {
        var javaMap = new HashMap<String, Object>();
        variables.keySet().forEach(key -> javaMap.put(key, variables.get(key)));
        taskService.complete(taskId, Variables.fromMap(javaMap));
    }

    @Delete("/{taskId}")
    @Operation(
            summary = "Complete Task",
            description = "Completes the given task without any additional data.",
            operationId = "deleteTask")
    public void complete(@PathVariable String taskId) {
        taskService.complete(taskId);
    }
}
