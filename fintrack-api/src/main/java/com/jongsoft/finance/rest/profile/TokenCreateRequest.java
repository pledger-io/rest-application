package com.jongsoft.finance.rest.profile;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@Serdeable.Deserializable
public class TokenCreateRequest {

    private String description;
    private LocalDate expires;

    public String getDescription() {
        return description;
    }

    public LocalDate getExpires() {
        return expires;
    }

}
