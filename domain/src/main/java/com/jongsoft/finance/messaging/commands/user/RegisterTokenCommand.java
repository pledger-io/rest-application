package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.core.ApplicationEvent;

import java.time.LocalDateTime;

public record RegisterTokenCommand(String username, String refreshToken, LocalDateTime expireDate)
        implements ApplicationEvent {
}
