package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable.Deserializable
record ImporterCreateRequest(
        @NotNull
        @NotBlank
        String configuration,
        @NotNull
        @NotBlank
        String uploadToken) {
}
