package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.core.SettingType;
import com.jongsoft.finance.domain.core.events.SettingUpdatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public class Setting {

    private final String name;
    private final SettingType type;
    private String value;

    public void update(String value) {
        if (!Objects.equals(this.value, value)) {
            switch (type) {
                case NUMBER -> new BigDecimal(value);
                case FLAG -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Value is not a valid setting for a boolean " + value);
                    }
                }
                case DATE -> LocalDate.parse(value);
            }

            this.value = value;
            EventBus.getBus().send(new SettingUpdatedEvent(
                    this,
                    name,
                    value));
        }
    }

}
