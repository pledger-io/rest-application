package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record SettingUpdatedEvent(String setting, String value) implements ApplicationEvent {

    public static void settingUpdated(String setting, String value) {
        new SettingUpdatedEvent(setting, value).publish();
    }
}
