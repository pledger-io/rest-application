package com.jongsoft.finance.rest.profile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Setter;

@Setter
class MultiFactorRequest {

    @NotNull
    @Size(min = 4, max = 8)
    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

}
