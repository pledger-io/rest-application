package com.jongsoft.finance.bpmn.process;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProcessTask {

    private final Task task;
    private final ProcessEngine processEngine;

    ProcessTask(Task task, ProcessEngine processEngine) {
        this.task = task;
        this.processEngine = processEngine;
    }

    @SuppressWarnings("unchecked")
    public <T> ProcessTask verifyVariable(String variable, Consumer<T> assertions) {
        var value = (T) processEngine.getTaskService()
                .getVariable(task.getId(), variable);

        Assertions.assertThat(value)
                .as("Variable %s is not set", variable)
                .isNotNull();

        assertions.accept(value);
        return this;
    }

    public <T> ProcessTask updateVariable(String variable, Function<T, T> mapping) {
        return updateVariable(variable, variable, mapping);
    }

    public <T> ProcessTask updateVariable(String variable, String outputVariable, Function<T, T> mapping) {
        @SuppressWarnings("unchecked")
        var value = (T) processEngine.getTaskService()
                .getVariable(task.getId(), variable);

        Assertions.assertThat(value)
                .as("Variable %s is not set", variable)
                .isNotNull();

        processEngine.getTaskService()
                .setVariable(
                        task.getId(),
                        outputVariable,
                        mapping.apply(value));
        return this;
    }

    public void complete() {
        processEngine.getTaskService()
                .complete(task.getId());
    }

    public void complete(Map<String, Object> variables) {
        processEngine.getTaskService()
                .complete(task.getId(), variables);
    }
}
