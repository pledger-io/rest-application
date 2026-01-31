package com.jongsoft.finance.exporter.domain.service.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TransactionTypeIndicator(
        @JsonProperty("deposit") String deposit, @JsonProperty("credit") String credit) {}
