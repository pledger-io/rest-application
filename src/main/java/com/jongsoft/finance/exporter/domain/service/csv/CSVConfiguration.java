package com.jongsoft.finance.exporter.domain.service.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jongsoft.finance.exporter.domain.model.ImporterConfiguration;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record CSVConfiguration(
        @JsonProperty("has-headers") boolean headers,
        @JsonProperty("date-format") String dateFormat,
        @JsonProperty("delimiter") String delimiter,
        @JsonProperty("custom-indicator") TransactionTypeIndicator transactionTypeIndicator,
        @JsonProperty("column-roles") List<ColumnRole> columnRoles)
        implements ImporterConfiguration {}
