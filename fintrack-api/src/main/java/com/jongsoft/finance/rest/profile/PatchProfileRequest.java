package com.jongsoft.finance.rest.profile;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

@Setter
@Introspected
public class PatchProfileRequest {

    private String theme;
    private String currency;
    private String password;

    public String theme() {
        return theme;
    }

    public String currency() {
        return currency;
    }

    public String password() {
        return password;
    }
}
