package com.jongsoft.finance.exporter.domain.model;

import com.jongsoft.finance.core.domain.model.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UserTask(String id, String type, ProcessVariable properties) {}
