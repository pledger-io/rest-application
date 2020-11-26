package com.jongsoft.finance.rest.security;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Introspected
class AuthenticationRequest implements io.micronaut.security.authentication.AuthenticationRequest<String, String> {

    @Email
    @NotNull
    private String username;

    @NotNull
    private String password;

    public AuthenticationRequest(@Email @NotNull final String username, @NotNull final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getIdentity() {
        return username;
    }

    @Override
    public String getSecret() {
        return password;
    }

}
