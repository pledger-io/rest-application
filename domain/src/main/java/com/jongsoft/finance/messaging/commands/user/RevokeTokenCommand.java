package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.core.ApplicationEvent;

public record RevokeTokenCommand(String token) implements ApplicationEvent {
}
