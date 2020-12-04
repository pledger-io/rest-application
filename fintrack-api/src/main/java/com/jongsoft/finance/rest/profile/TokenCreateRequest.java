package com.jongsoft.finance.rest.profile;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;

@Introspected
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
