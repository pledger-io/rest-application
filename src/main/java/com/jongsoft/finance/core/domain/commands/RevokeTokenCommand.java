package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record RevokeTokenCommand(String token) implements ApplicationEvent {

    public static void tokenRevoked(String token) {
        new RevokeTokenCommand(token).publish();
    }
}
