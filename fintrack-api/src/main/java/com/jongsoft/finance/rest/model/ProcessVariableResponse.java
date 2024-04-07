package com.jongsoft.finance.rest.model;

import io.micronaut.serde.annotation.Serdeable;
import org.camunda.bpm.engine.runtime.VariableInstance;

@Serdeable.Serializable
public class ProcessVariableResponse {

    private final VariableInstance wrapped;

    public ProcessVariableResponse(VariableInstance wrapped) {
        this.wrapped = wrapped;
    }

    public String getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public Object getValue() {
        return wrapped.getValue();
    }

}
