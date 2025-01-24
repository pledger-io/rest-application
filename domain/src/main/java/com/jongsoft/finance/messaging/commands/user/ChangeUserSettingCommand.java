package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeUserSettingCommand(String username, Type type, String value)
        implements ApplicationEvent {
    public enum Type {
        THEME, CURRENCY
    }

    public static void userSettingChanged(String username, Type type, String value) {
        new ChangeUserSettingCommand(username, type, value)
                .publish();
    }
}
