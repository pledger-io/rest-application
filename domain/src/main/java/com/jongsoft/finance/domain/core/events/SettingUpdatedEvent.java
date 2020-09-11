package com.jongsoft.finance.domain.core.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class SettingUpdatedEvent implements ApplicationEvent {

    private final String setting;
    private final String value;

    public SettingUpdatedEvent(Object source, String setting, String value) {
        this.setting = setting;
        this.value = value;
    }

}
