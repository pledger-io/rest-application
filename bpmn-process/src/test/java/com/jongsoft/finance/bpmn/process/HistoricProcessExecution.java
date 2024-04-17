package com.jongsoft.finance.bpmn.process;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

import java.util.function.Consumer;

public class HistoricProcessExecution implements ProcessTestExtension.ProcessExecution<HistoricProcessExecution> {

    private final ProcessEngine processEngine;
    private final HistoricProcessInstance processInstance;

    HistoricProcessExecution(ProcessEngine processEngine, HistoricProcessInstance processInstance) {
        this.processEngine = processEngine;
        this.processInstance = processInstance;
    }

    @Override
    public HistoricProcessExecution obtainChildProcess(String processKey) {
        var subProcess = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .superProcessInstanceId(processInstance.getId())
                .processDefinitionKey(processKey)
                .singleResult();

        Assertions.assertThat(subProcess)
                .as("Sub process '%s' not found", processKey)
                .isNotNull();

        return new HistoricProcessExecution(processEngine, subProcess);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> HistoricProcessExecution yankVariable(String variableName, Consumer<Y> consumer) {
        var variable = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getId())
                .variableName(variableName)
                .singleResult();

        Assertions.assertThat(variable)
                .as("Variable %s not found", variableName)
                .isNotNull();

        consumer.accept((Y) variable.getValue());
        return this;
    }

    public HistoricProcessExecution verifySuccess() {
        var successEndEvent = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .activityType("noneEndEvent")
                .singleResult();

        Assertions.assertThat(successEndEvent)
                .as("Process '%s' not completed successfully", processInstance.getProcessDefinitionKey())
                .isNotNull();

        return this;
    }

    public HistoricProcessExecution verifyErrorCompletion() {
        var successEndEvent = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .activityType("errorEndEvent")
                .singleResult();

        Assertions.assertThat(successEndEvent)
                .as("Process '%s' not completed with error", processInstance.getProcessDefinitionKey())
                .isNotNull();

        return this;
    }

    @Override
    public HistoricProcessExecution verifyCompleted() {
        Assertions.assertThat(processInstance.getEndTime())
                .as("Process '%s' not completed", processInstance.getProcessDefinitionKey())
                .isNotNull();

        return this;
    }
}
