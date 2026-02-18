package com.jongsoft.finance.core.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.micronaut.serde.annotation.Serdeable;

import java.io.Serializable;

@Serdeable
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "_type")
public interface ProcessVariable extends Serializable {}
