package com.jongsoft.finance.rest.model.runtime;

import com.jongsoft.finance.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ListVariable(List<ProcessVariable> content) implements ProcessVariable {}
