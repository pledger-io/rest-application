package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

public record ChangeUserSettingCommand(UserIdentifier username, Type type, String value)
        implements ApplicationEvent {
    public enum Type {
        THEME,
        CURRENCY
    }

    public static void userSettingChanged(UserIdentifier username, Type type, String value) {
        new ChangeUserSettingCommand(username, type, value).publish();
    }
}
