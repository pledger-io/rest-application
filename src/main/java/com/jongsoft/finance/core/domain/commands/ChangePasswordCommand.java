package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

public record ChangePasswordCommand(UserIdentifier username, String password)
        implements ApplicationEvent {

    public static void passwordChanged(UserIdentifier username, String password) {
        new ChangePasswordCommand(username, password).publish();
    }
}
