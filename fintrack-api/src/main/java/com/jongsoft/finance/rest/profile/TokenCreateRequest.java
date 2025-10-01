package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable.Deserializable
public record TokenCreateRequest(String description, LocalDate expires) {}
