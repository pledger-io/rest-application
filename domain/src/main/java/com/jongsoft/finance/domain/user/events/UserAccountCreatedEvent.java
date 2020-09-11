package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserAccountCreatedEvent implements ApplicationEvent {

    private final String username;
    private final String password;

    public UserAccountCreatedEvent(Object source, String username, String password) {
        this.username = username;
        this.password = password;
    }

}
