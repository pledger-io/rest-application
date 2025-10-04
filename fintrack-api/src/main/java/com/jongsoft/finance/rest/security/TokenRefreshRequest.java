package com.jongsoft.finance.rest.security;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Serdeable.Deserializable
public class TokenRefreshRequest {

    @NotBlank
    @Schema(
            description = "The refresh token that, this can be obtained from the JWT provided after"
                    + " login.",
            required = true,
            implementation = String.class)
    private String token;

    public String getToken() {
        return token;
    }
}
