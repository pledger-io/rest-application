package com.jongsoft.finance.rest.model.runtime;

import com.jongsoft.finance.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class VariableMap {
    private Map<String, ProcessVariable> variables;

    public VariableMap(Map<String, ProcessVariable> variables) {
        this.variables = variables;
    }

    public Map<String, ProcessVariable> getVariables() {
        return variables;
    }
}
