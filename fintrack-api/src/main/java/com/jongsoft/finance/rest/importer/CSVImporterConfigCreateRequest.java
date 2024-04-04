package com.jongsoft.finance.rest.importer;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Serdeable.Deserializable
record CSVImporterConfigCreateRequest(
        @NotBlank
        @Schema(description = "The type of importer that is to be used")

        String type,
        @Schema(description = "The name of the configuration")

        @NotBlank
        String name,
        @Schema(description = "The file code to get the contents of the configuration")
        @NotBlank
        String fileCode) {
}
