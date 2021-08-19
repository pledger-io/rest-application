package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.rest.model.ProcessResponse;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Process Engine")
@Controller("/api/runtime-process")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RuntimeResource {

    private static final String KEY_USERNAME = "username";
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final AuthenticationFacade authenticationFacade;

    @Put("/{processDefinitionKey}/start")
    @Operation(
            summary = "Create Process",
            description = "Creates and executes a new process for the selected definition, with the provided map as parameters",
            operationId = "startProcess"
    )
    public Publisher<ProcessResponse> startProcess(
            @PathVariable String processDefinitionKey,
            @Body Map<String, Object> parameters) {

        var response = Mono.<ProcessInstance>create(emitter -> {
            var instanceBuilder = runtimeService.createProcessInstanceByKey(processDefinitionKey);
            parameters.forEach(instanceBuilder::setVariable);

            if (parameters.containsKey("businessKey")) {
                instanceBuilder.businessKey(parameters.get("businessKey").toString());
            }

            instanceBuilder.setVariable(KEY_USERNAME, authenticationFacade.authenticated());

            emitter.success(instanceBuilder.execute());
        });

        return response.map(process -> historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getProcessInstanceId()).singleResult())
                .map(ProcessResponse::new);
    }

    @Get("/{processDefinitionKey}")
    @Operation(
            summary = "Process History",
            description = "Lists the historic executions for the provided process definition key",
            operationId = "getProcessHistory"
    )
    public Publisher<ProcessResponse> history(@PathVariable String processDefinitionKey) {
        var result = Collections.List(historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .variableValueEquals(KEY_USERNAME, authenticationFacade.authenticated())
                .orderByProcessInstanceStartTime().desc()
                .list());

        return Flux.fromIterable(result.map(ProcessResponse::new));
    }

    @Get("/{processDefinitionKey}/{businessKey}")
    @Operation(
            summary = "Process History for key",
            description = "List the history executions for the provided definition key, but only once with matching business key",
            operationId = "getProcessHistoryByBusinessKey"
    )
    public Publisher<ProcessResponse> history(
            @PathVariable String processDefinitionKey,
            @PathVariable String businessKey) {
        var result = Collections.List(historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceBusinessKey(businessKey)
                .variableValueEquals(KEY_USERNAME, authenticationFacade.authenticated())
                .orderByProcessInstanceStartTime().desc()
                .list());

        return Flux.fromIterable(result.map(ProcessResponse::new));
    }

    @Status(HttpStatus.NO_CONTENT)
    @Delete("/{processDefinitionKey}/{businessKey}/{instanceId}")
    @Operation(
            summary = "Delete Process",
            description = "Removes a active process from the execution list",
            operationId = "deleteProcess",
            responses = @ApiResponse(responseCode = "204")
    )
    public void deleteProcess(
            @PathVariable String processDefinitionKey,
            @PathVariable String businessKey,
            @PathVariable String instanceId) {
        runtimeService.deleteProcessInstance(instanceId, "User termination");
    }

    @Get("/clean-up")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Force History Clean",
            description = "Trigger a history clean-up job to run",
            operationId = "triggerHistoryCleaning",
            responses = @ApiResponse(responseCode = "204")
    )
    public void cleanHistory() {
        historyService.cleanUpHistoryAsync(true);
    }

}
