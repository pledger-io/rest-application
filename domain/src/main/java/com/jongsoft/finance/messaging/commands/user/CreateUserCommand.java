package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateUserCommand(String username, String password) implements ApplicationEvent {
}
