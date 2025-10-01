package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Serdeable.Deserializable
record MultiFactorRequest(@NotNull @Size(min = 4, max = 8) String verificationCode) {}
