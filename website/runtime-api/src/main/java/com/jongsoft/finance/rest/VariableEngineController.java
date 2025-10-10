package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.model.runtime.VariableResponse;

import io.micronaut.http.annotation.Controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class VariableEngineController implements VariableEngineApi {

    private final Logger logger;
    private final RuntimeService runtimeService;

    VariableEngineController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
        this.logger = LoggerFactory.getLogger(VariableEngineController.class);
    }

    @Override
    public List<VariableResponse> getVariableById(
            String processDefinition, String businessKey, String instanceId, String variableId) {
        logger.info("Getting variable {} for process instance {}.", variableId, instanceId);

        return runtimeService
                .createVariableInstanceQuery()
                .processInstanceIdIn(instanceId)
                .variableName(variableId)
                .list()
                .stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public List<VariableResponse> getVariables(
            String businessKey, String processDefinition, String instanceId) {
        logger.info("Getting variables for process instance {}.", instanceId);
        return runtimeService
                .createVariableInstanceQuery()
                .processInstanceIdIn(instanceId)
                .list()
                .stream()
                .map(this::convert)
                .toList();
    }

    private VariableResponse convert(VariableInstance variable) {
        var response = new VariableResponse();
        response.id(variable.getId());
        response.name(variable.getName());
        response.value(variable.getValue());
        return response;
    }
}
