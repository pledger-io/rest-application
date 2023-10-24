package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDate;

@Serdeable.Deserializable
public class TokenCreateRequest {

    private String description;
    private LocalDate expires;

    public TokenCreateRequest() {
    }

    TokenCreateRequest(String description, LocalDate expires) {
        this.description = description;
        this.expires = expires;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpires() {
        return expires;
    }

}
