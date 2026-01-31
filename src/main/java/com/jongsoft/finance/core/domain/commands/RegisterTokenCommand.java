package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDateTime;

public record RegisterTokenCommand(String username, String refreshToken, LocalDateTime expireDate)
        implements ApplicationEvent {

    public static void tokenRegistered(
            String username, String refreshToken, LocalDateTime expireDate) {
        new RegisterTokenCommand(username, refreshToken, expireDate).publish();
    }
}
