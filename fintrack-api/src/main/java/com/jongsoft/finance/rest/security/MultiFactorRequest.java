package com.jongsoft.finance.rest.security;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Serdeable.Deserializable
record MultiFactorRequest(
        @NotNull
                @Size(min = 4, max = 8)
                @Schema(
                        description = "The 2-factor verification code from a hardware device.",
                        required = true,
                        pattern = "[\\d]{6}",
                        implementation = String.class)
                String verificationCode) {}
