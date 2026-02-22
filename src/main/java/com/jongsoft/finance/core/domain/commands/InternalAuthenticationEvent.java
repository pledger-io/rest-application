package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record InternalAuthenticationEvent(String username) implements ApplicationEvent {

    public static void authenticate(String username) {
        new InternalAuthenticationEvent(username).publish();
    }
}
