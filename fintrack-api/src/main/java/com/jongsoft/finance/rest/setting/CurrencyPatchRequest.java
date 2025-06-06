package com.jongsoft.finance.rest.setting;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Deserializable
public record CurrencyPatchRequest(Integer decimalPlaces, Boolean enabled) {}
