package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable.Deserializable
record CSVImporterConfigCreateRequest(
        @NotBlank
        String name,
        @NotBlank
        String fileCode) {
}
