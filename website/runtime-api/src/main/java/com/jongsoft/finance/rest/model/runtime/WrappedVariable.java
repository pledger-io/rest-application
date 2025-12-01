package com.jongsoft.finance.rest.model.runtime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jongsoft.finance.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record WrappedVariable<T>(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_type") T value)
        implements ProcessVariable {}
