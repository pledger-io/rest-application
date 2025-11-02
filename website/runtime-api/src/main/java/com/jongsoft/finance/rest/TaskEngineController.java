package com.jongsoft.finance.rest;

import com.jongsoft.finance.ProcessVariable;
import com.jongsoft.finance.rest.model.runtime.*;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
class TaskEngineController implements TaskEngineApi {

    private final Logger logger;
    private final TaskService taskService;

    TaskEngineController(TaskService taskService) {
        this.taskService = taskService;
        this.logger = LoggerFactory.getLogger(TaskEngineController.class);
    }

    @Override
    public HttpResponse<Void> completeTask(
            String processDefinition,
            String instanceId,
            String businessKey,
            String taskId,
            VariableMap taskVariableMap) {
        logger.info("Completing task {} for process instance {}.", taskId, instanceId);

        var variables = new HashMap<String, Object>();
        for (var entry : taskVariableMap.getVariables().entrySet()) {
            variables.put(entry.getKey(), from(entry.getValue()));
        }
        taskService.complete(taskId, variables);
        return HttpResponse.noContent();
    }

    @Override
    public HttpResponse<Void> deleteTask(
            String processDefinition, String instanceId, String businessKey, String taskId) {
        logger.info("Deleting task {} for process instance {}.", taskId, instanceId);
        taskService.complete(taskId);
        return HttpResponse.noContent();
    }

    @Override
    public TaskResponse getTaskById(
            String processDefinition, String instanceId, String businessKey, String taskId) {
        logger.info("Getting task {} for process instance {}.", taskId, instanceId);

        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return convert(task);
    }

    @Override
    public VariableMap getTaskVariables(
            String processDefinition,
            String businessKey,
            String instanceId,
            String taskId,
            String variable) {
        logger.info("Getting variables for task {} for process instance {}.", taskId, instanceId);

        var map = new HashMap<String, ProcessVariable>();
        var variables = variable != null
                ? Map.of(variable, taskService.getVariable(taskId, variable))
                : taskService.getVariables(taskId);
        for (var entry : variables.entrySet()) {
            if (entry.getKey().equals("username")) {
                continue;
            }
            map.put(entry.getKey(), convert(entry.getValue()));
        }

        return new VariableMap(map);
    }

    @Override
    public List<TaskResponse> getTasks(
            String processDefinition, String businessKey, String instanceId) {
        logger.info("Getting tasks for process instance {}.", instanceId);
        return taskService
                .createTaskQuery()
                .processDefinitionKey(processDefinition)
                .processInstanceId(instanceId)
                .initializeFormKeys()
                .list()
                .stream()
                .map(this::convert)
                .toList();
    }

    private TaskResponse convert(Task task) {
        var response = new TaskResponse(task.getId(), task.getName());
        response.definition(task.getTaskDefinitionKey());
        response.form(task.getFormKey());
        response.created(
                ZonedDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.of("UTC")));
        return response;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ProcessVariable convert(Object value) {
        return switch (value) {
            case Collection list ->
                new ListVariable(list.stream().map(this::convert).toList());
            case ProcessVariable variable -> variable;
            case null -> null;
            default -> new WrappedVariable(value);
        };
    }

    private Object from(Object processVariable) {
        return switch (processVariable) {
            case ListVariable list -> list.content().stream().map(this::from).toList();
            case WrappedVariable<?> wrapped -> wrapped.value();
            default -> processVariable;
        };
    }
}
