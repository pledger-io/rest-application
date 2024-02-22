package com.jongsoft.finance.bpmn.process;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class RunningProcessExecution implements ProcessTestExtension.ProcessExecution<RunningProcessExecution> {

    private final ProcessEngine processEngine;
    private final ProcessInstance processInstance;

    RunningProcessExecution(ProcessEngine processEngine, String processKey, Map<String, Object> variables) {
        this.processEngine = processEngine;

        log.debug("Running process with key: {}", processKey);

        Map<String, Object> expandedVariables = new HashMap<>(variables);
        expandedVariables.put("username", "test-user");

        processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processKey, expandedVariables);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RunningProcessExecution yankVariable(String variableName, Consumer<T> consumer) {
        var variable = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getProcessInstanceId())
                .variableName(variableName)
                .singleResult();

        Assertions.assertThat(variable)
                .as("Variable %s not found", variableName)
                .isNotNull();

        consumer.accept((T) variable.getValue());
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> RunningProcessExecution yankVariables(String variableName, Consumer<ListAssert<T>> consumer) {
        var variable = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceIdIn(processInstance.getProcessInstanceId())
                .variableName(variableName)
                .list();

        Assertions.assertThat(variable)
                .as("Variable %s not found", variableName)
                .isNotNull();

        consumer.accept(Assertions.assertThat(variable.stream()
                .map(v -> (T) v.getValue())
                .toList()));
        return this;
    }

    @Override
    public HistoricProcessExecution obtainChildProcess(String processKey) {
        var subProcess = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .superProcessInstanceId(processInstance.getProcessInstanceId())
                .processDefinitionKey(processKey)
                .singleResult();

        Assertions.assertThat(subProcess)
                .as("Sub process '%s' not found", processKey)
                .isNotNull();

        return new HistoricProcessExecution(processEngine, subProcess);
    }

    public RunningProcessExecution forceJob(String activityId) {
        var job = processEngine.getManagementService()
                .createJobQuery()
                .processInstanceId(processInstance.getProcessInstanceId())
                .activityId(activityId)
                .active()
                .singleResult();

        Assertions.assertThat(job)
                .as("Job for activity '%s' not found", activityId)
                .isNotNull()
                .satisfies(p -> {
                    Assertions.assertThat(p.getDuedate())
                            .as("Job for activity '%s' is not in the future", activityId)
                            .isInTheFuture();
                });

        processEngine.getManagementService()
                .executeJob(job.getId());

        return this;
    }

    @Override
    public RunningProcessExecution verifyCompleted() {
        processEngine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .list()
                .forEach(pi -> {
                    if (!pi.isEnded()) {
                        processEngine.getHistoryService()
                                .createHistoricActivityInstanceQuery()
                                .processInstanceId(pi.getId())
                                .unfinished()
                                .list()
                                .forEach(task -> {
                                    log.error("Activity [{}:'{}'] is not completed", task.getActivityId(), task.getActivityName());
                                });
                    }
                    Assertions.assertThat(pi.isEnded())
                            .as("Process instance %s is not ended", pi.getId())
                            .isTrue();
                });

        return this;
    }

    public RunningProcessExecution verifyPendingActivity(String activityId) {
        var task = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getProcessInstanceId())
                .activityId(activityId)
                .unfinished()
                .singleResult();

        Assertions.assertThat(task)
                .as("Task %s not active", activityId)
                .isNotNull();

        return this;
    }

    public ProcessTask task(String taskId) {
        var processTask = processEngine.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey(taskId)
                .processInstanceId(processInstance.getProcessInstanceId())
                .singleResult();
        Assertions.assertThat(processTask)
                .as("Task %s not active", taskId)
                .isNotNull();

        return new ProcessTask(processTask, processEngine);
    }
}
