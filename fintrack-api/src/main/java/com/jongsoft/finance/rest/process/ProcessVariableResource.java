package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessVariableResponse;
import com.jongsoft.lang.API;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;

@Tag(name = "Process Engine")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/runtime-process/{processDefinitionKey}/{businessKey}/{instanceId}/variables")
public class ProcessVariableResource {

    private final HistoryService historyService;

    public ProcessVariableResource(ProcessEngine processEngine) {
        this.historyService = processEngine.getHistoryService();
    }

    @Get
    @Operation(
            summary = "Get variables",
            description = "This operation lists all process variables available for the provided process",
            operationId = "getVariables"
    )
    public Flowable<ProcessVariableResponse> variables(
            @PathVariable String processDefinitionKey,
            @PathVariable String instanceId) {
        var result = API.List(historyService.createHistoricVariableInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceId(instanceId)
                .list());

        return Flowable.fromIterable(result.map(ProcessVariableResponse::new));
    }

    @Get("/{variable}")
    @Operation(
            summary = "Get variable",
            description = "This operation lists variables of a given name for a process",
            operationId = "getVariable"
    )
    public Flowable<ProcessVariableResponse> variable(
            @PathVariable String processDefinitionKey,
            @PathVariable String instanceId,
            @PathVariable String variable) {
        var result = API.List(historyService.createHistoricVariableInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceId(instanceId)
                .variableName(variable)
                .list());

        return Flowable.fromIterable(result.map(ProcessVariableResponse::new));
    }

}