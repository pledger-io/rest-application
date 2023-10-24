package com.jongsoft.finance.rest.localization;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
class LanguageResponse {

    private String text;

    public LanguageResponse(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
