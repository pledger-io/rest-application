package com.jongsoft.finance.rest.setting;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Serdeable.Deserializable
public class SettingUpdateRequest {

    private String value;

    public String getValue() {
        return value;
    }

}
