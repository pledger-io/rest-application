package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;

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
