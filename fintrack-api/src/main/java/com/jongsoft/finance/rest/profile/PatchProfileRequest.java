package com.jongsoft.finance.rest.profile;

import lombok.Setter;

@Setter
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
