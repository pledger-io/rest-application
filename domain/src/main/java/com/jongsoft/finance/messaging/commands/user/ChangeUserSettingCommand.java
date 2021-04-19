package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.core.ApplicationEvent;

public record ChangeUserSettingCommand(String username, Type type, String value)
        implements ApplicationEvent {
    public enum Type {
        THEME, CURRENCY
    }
}
