package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RevokeTokenCommand(String token) implements ApplicationEvent {

    public static void tokenRevoked(String token) {
        new RevokeTokenCommand(token)
                .publish();
    }
}
