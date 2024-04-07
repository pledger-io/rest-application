package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessVariableResponse;
import com.jongsoft.lang.Collections;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.RuntimeService;

import java.util.List;

@Tag(name = "Process Engine")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/runtime-process/{processDefinitionKey}/{businessKey}/{instanceId}/variables")
public class ProcessVariableResource {

    private final RuntimeService runtimeService;

    public ProcessVariableResource(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Get
    @Operation(
            summary = "Get variables",
            description = "This operation lists all process variables available for the provided process",
            operationId = "getVariables"
    )
    public List<ProcessVariableResponse> variables(
            @PathVariable String processDefinitionKey,
            @PathVariable String instanceId) {
        return Collections.List(runtimeService.createVariableInstanceQuery()
                        .processInstanceIdIn(instanceId)
                        .list())
                .map(ProcessVariableResponse::new)
                .toJava();
    }

    @Get("/{variable}")
    @Operation(
            summary = "Get variable",
            description = "This operation lists variables of a given name for a process",
            operationId = "getVariable"
    )
    public List<ProcessVariableResponse> variable(
            @PathVariable String processDefinitionKey,
            @PathVariable String instanceId,
            @PathVariable String variable) {
        return Collections.List(runtimeService.createVariableInstanceQuery()
                        .processInstanceIdIn(instanceId)
                        .variableName(variable)
                        .list())
                .map(ProcessVariableResponse::new)
                .toJava();
    }

}
