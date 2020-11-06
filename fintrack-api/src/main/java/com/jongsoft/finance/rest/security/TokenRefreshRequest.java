package com.jongsoft.finance.rest.security;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class TokenRefreshRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

}
