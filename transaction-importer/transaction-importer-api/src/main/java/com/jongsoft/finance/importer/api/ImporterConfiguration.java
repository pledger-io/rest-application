package com.jongsoft.finance.importer.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public interface ImporterConfiguration {}
