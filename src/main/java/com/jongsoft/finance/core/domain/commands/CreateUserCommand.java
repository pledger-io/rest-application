package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateUserCommand(String username, String password) implements ApplicationEvent {

    public static void userCreated(String username, String password) {
        new CreateUserCommand(username, password).publish();
    }
}
