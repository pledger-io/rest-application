package com.jongsoft.finance.rest.setting;

import io.micronaut.core.annotation.Introspected;
import lombok.Setter;

@Setter
@Introspected
public class SettingUpdateRequest {

    private String value;

    public String getValue() {
        return value;
    }

}
