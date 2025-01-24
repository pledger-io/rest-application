package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangePasswordCommand(UserIdentifier username, String password) implements ApplicationEvent {

    public static void passwordChanged(UserIdentifier username, String password) {
        new ChangePasswordCommand(username, password)
                .publish();
    }
}
