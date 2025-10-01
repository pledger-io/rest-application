package com.jongsoft.finance.rest.transaction;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
record GroupRenameRequest(
        @NotNull
                @NotBlank
                @Schema(description = "The new name of the group", example = "My renamed group")
                String name) {}
