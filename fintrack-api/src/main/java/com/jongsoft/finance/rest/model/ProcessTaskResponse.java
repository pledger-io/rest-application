package com.jongsoft.finance.rest.model;

import io.micronaut.serde.annotation.Serdeable;

import org.camunda.bpm.engine.task.Task;

import java.util.Date;

@Serdeable.Serializable
public class ProcessTaskResponse {

    private final Task wrapped;

    public ProcessTaskResponse(Task wrapped) {
        this.wrapped = wrapped;
    }

    public String getId() {
        return wrapped.getId();
    }

    public String getDefinition() {
        return wrapped.getTaskDefinitionKey();
    }

    public Date getCreated() {
        return wrapped.getCreateTime();
    }

    public String getForm() {
        return wrapped.getFormKey();
    }

    public String getName() {
        return wrapped.getName();
    }
}
