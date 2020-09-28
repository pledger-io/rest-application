package com.jongsoft.finance.rest.setting;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Introspected
@NoArgsConstructor
@AllArgsConstructor
public class SettingUpdateRequest {

    private String value;

    public String getValue() {
        return value;
    }

}
