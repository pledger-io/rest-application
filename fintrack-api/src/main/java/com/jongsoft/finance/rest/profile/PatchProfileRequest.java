package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
public record PatchProfileRequest(String theme, String currency, String password) {}
