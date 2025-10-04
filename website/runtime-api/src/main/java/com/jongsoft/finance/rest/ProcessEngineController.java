package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.rest.model.runtime.ProcessInstanceResponse;
import com.jongsoft.finance.rest.model.runtime.ProcessInstanceResponseState;
import com.jongsoft.finance.security.AuthenticationFacade;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Controller
class ProcessEngineController implements ProcessEngineApi {
    private static final String KEY_USERNAME = "username";

    private final RuntimeService runtimeService;
    private final AuthenticationFacade authenticationFacade;
    private final Logger logger;

    public ProcessEngineController(
            RuntimeService runtimeService, AuthenticationFacade authenticationFacade) {
        this.runtimeService = runtimeService;
        this.authenticationFacade = authenticationFacade;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public HttpResponse<Void> deleteProcessInstance(
            String processDefinition, String businessKey, String instanceId) {
        logger.info("Deleting the process instance with identifier {}.", instanceId);

        runtimeService.deleteProcessInstance(instanceId, "User termination");
        return HttpResponse.noContent();
    }

    @Override
    public ProcessInstanceResponse getProcessInstance(
            String processDefinition, String businessKey, String instanceId) {
        logger.info("Getting processes for instance {}.", instanceId);

        var processInstance = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinition)
                .processInstanceId(instanceId)
                .variableValueEquals(KEY_USERNAME, authenticationFacade.authenticated())
                .singleResult();
        return convert(processInstance);
    }

    @Override
    public List<@Valid ProcessInstanceResponse> getProcessInstances(String processDefinition) {
        logger.info("Listing all processes for definition {}.", processDefinition);

        return runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinition)
                .variableValueEquals(KEY_USERNAME, authenticationFacade.authenticated())
                .orderByProcessInstanceId()
                .desc()
                .list()
                .stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public List<@Valid ProcessInstanceResponse> getProcessInstancesByBusinessKey(
            String processDefinition, String businessKey) {
        logger.info(
                "Listing all processes for definition {} and business key {}.",
                processDefinition,
                businessKey);

        return runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinition)
                .processInstanceBusinessKey(businessKey)
                .variableValueEquals(KEY_USERNAME, authenticationFacade.authenticated())
                .orderByProcessInstanceId()
                .desc()
                .list()
                .stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public HttpResponse<@Valid ProcessInstanceResponse> startProcessInstance(
            String processDefinition, Map<String, Object> parameters) {
        logger.info("Starting a new process for {}.", processDefinition);

        var instanceBuilder = runtimeService.createProcessInstanceByKey(processDefinition);
        parameters.forEach(instanceBuilder::setVariable);

        if (parameters.containsKey("businessKey")) {
            instanceBuilder.businessKey(parameters.get("businessKey").toString());
        }
        instanceBuilder.setVariable(KEY_USERNAME, authenticationFacade.authenticated());

        return HttpResponse.created(convert(instanceBuilder.execute()));
    }

    private ProcessInstanceResponse convert(ProcessInstance processInstance) {
        if (processInstance == null) {
            throw StatusException.notFound("No process instance found.");
        }
        return new ProcessInstanceResponse(
                processInstance.getProcessInstanceId(),
                processInstance.getProcessDefinitionId(),
                processInstance.getBusinessKey(),
                processInstance.isEnded()
                        ? ProcessInstanceResponseState.COMPLETED
                        : ProcessInstanceResponseState.ACTIVE);
    }
}
