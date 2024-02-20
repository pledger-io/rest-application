package com.jongsoft.finance.bpmn.process;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProcessExecution {

    private final ProcessEngine processEngine;
    private final ProcessInstance processInstance;

    ProcessExecution(ProcessEngine processEngine, String processKey, Map<String, Object> variables) {
        this.processEngine = processEngine;

        log.debug("Running process with key: {}", processKey);

        Map<String, Object> expandedVariables = new HashMap<>(variables);
        expandedVariables.put("username", "test-user");

        processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(processKey, expandedVariables);
    }

    public ProcessExecution verifyCompleted() {
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
                                    log.error("Activity {} is not completed", task.getActivityName());
                                });
                    }
                    Assertions.assertThat(pi.isEnded())
                            .as("Process instance %s is not ended", pi.getId())
                            .isTrue();
                });

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
