package com.jongsoft.finance.rest.profile;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
