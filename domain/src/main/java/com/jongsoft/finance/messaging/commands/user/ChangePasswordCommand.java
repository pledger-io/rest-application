package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangePasswordCommand(String username, String password) implements ApplicationEvent {

    public static void passwordChanged(String username, String password) {
        new ChangePasswordCommand(username, password)
                .publish();
    }
}
