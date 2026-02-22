package com.jongsoft.finance.exporter.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jongsoft.finance.core.domain.model.ProcessVariable;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_type")
public interface ImporterConfiguration extends ProcessVariable {}
