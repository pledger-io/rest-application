package com.jongsoft.finance.serialized;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jongsoft.finance.importer.api.ImporterConfiguration;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ImportJobSettings(
        @JsonProperty
        ImporterConfiguration importConfiguration,
        @JsonProperty
        boolean applyRules,
        @JsonProperty
        boolean generateAccounts,
        @JsonProperty
        Long accountId) {}
