package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** The tag create request is used to add new tags to FinTrack */
@Serdeable.Deserializable
public record TagCreateRequest(
    @Schema(
            description = "The name of the tag to be created",
            implementation = String.class,
            example = "Car expenses",
            required = true)
        @NotNull
        @NotBlank
        String tag) {}
