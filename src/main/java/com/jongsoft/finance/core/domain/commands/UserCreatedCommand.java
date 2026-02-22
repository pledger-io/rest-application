package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record UserCreatedCommand(String username) implements ApplicationEvent {
    public static void userCreated(String username) {
        new UserCreatedCommand(username).publish();
    }
}
