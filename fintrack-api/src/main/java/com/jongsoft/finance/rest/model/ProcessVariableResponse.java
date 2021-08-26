package com.jongsoft.finance.rest.model;

import io.micronaut.core.annotation.Introspected;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

@Introspected
public class ProcessVariableResponse {

    private final HistoricVariableInstance wrapped;

    public ProcessVariableResponse(HistoricVariableInstance wrapped) {
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
