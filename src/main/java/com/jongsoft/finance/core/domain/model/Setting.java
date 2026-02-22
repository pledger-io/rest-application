package com.jongsoft.finance.core.domain.model;

import com.jongsoft.finance.core.domain.commands.SettingUpdatedEvent;

import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Introspected
public class Setting {

    private final String name;
    private final SettingType type;
    private String value;

    Setting(String name, SettingType type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public void update(String value) {
        if (!Objects.equals(this.value, value)) {
            switch (type) {
                case NUMBER -> new BigDecimal(value);
                case FLAG -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException(
                                "Value is not a valid setting for a boolean " + value);
                    }
                }
                case DATE -> LocalDate.parse(value);
            }

            this.value = value;
            SettingUpdatedEvent.settingUpdated(name, value);
        }
    }

    public String getName() {
        return name;
    }

    public SettingType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
