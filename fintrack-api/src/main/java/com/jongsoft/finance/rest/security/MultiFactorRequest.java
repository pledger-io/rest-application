package com.jongsoft.finance.rest.security;

import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Introspected
class MultiFactorRequest {

    @NotNull
    @Size(min = 4, max = 8)
    @Schema(
            description = "The 2-factor verification code from a hardware device.",
            required = true,
            pattern = "[\\d]{6}",
            implementation = String.class)
    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

}
