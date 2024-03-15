package com.jongsoft.finance.rest.localization;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
record LanguageResponse(String text) {
}
