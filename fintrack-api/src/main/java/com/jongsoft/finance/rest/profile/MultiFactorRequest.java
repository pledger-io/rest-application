package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Setter;

@Setter
@Serdeable.Deserializable
class MultiFactorRequest {

    @NotNull
    @Size(min = 4, max = 8)
    private String verificationCode;

    public String getVerificationCode() {
        return verificationCode;
    }

}
