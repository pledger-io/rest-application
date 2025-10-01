package com.jongsoft.finance.rest.category;

import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Serdeable.Deserializable
record CategoryCreateRequest(
        @NotNull @NotBlank @Size(max = 255) String name, @Size(max = 1024) String description) {}
