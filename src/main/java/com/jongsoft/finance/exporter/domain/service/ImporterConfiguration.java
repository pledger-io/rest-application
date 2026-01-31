package com.jongsoft.finance.exporter.domain.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public interface ImporterConfiguration {}
