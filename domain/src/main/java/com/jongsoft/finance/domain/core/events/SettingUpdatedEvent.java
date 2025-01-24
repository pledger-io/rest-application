package com.jongsoft.finance.domain.core.events;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record SettingUpdatedEvent(String setting, String value) implements ApplicationEvent {

    public static void settingUpdated(String setting, String value) {
        new SettingUpdatedEvent(setting, value)
                .publish();
    }

}
