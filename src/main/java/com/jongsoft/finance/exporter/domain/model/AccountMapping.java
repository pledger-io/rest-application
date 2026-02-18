package com.jongsoft.finance.exporter.domain.model;

import com.jongsoft.finance.core.domain.model.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AccountMapping(String name, Long accountId) implements ProcessVariable {}
