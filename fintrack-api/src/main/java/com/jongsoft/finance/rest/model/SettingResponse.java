package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.core.SettingType;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
public class SettingResponse {

    private String name;
    private String value;
    private SettingType type;

    public SettingResponse(String name, String value, SettingType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public SettingType getType() {
        return type;
    }
}
