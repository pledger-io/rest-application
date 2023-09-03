package com.jongsoft.finance.rest.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Introspected
class AuthenticationRequest implements io.micronaut.security.authentication.AuthenticationRequest<String, String> {

    @Email
    @NotNull
    @Schema(
            description = "The username, must be a valid e-mail address.",
            required = true,
            implementation = String.class,
            example = "me@example.com")
    private String username;

    @NotNull
    @Schema(
            description = "The password",
            required = true,
            implementation = String.class,
            example = "password123")
    private String password;

    public AuthenticationRequest(@Email @NotNull final String username, @NotNull final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    @JsonIgnore
    public String getIdentity() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getSecret() {
        return password;
    }

}
