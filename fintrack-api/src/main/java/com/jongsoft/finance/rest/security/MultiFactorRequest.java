package com.jongsoft.finance.rest.security;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Introspected
class MultiFactorRequest {

    @NotNull
    @Size(min = 4, max = 8)
    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

}
