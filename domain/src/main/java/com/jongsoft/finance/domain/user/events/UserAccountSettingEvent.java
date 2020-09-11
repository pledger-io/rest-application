package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserAccountSettingEvent implements ApplicationEvent {

    public enum Type {
        THEME, CURRENCY
    }

    private final String username;
    private final Type typeOfSetting;
    private final String value;

    public UserAccountSettingEvent(Object source, String username, Type typeOfSetting, String value) {
        this.username = username;
        this.typeOfSetting = typeOfSetting;
        this.value = value;
    }

}
