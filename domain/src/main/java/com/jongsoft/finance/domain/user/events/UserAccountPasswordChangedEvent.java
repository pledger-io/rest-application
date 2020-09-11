package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserAccountPasswordChangedEvent implements ApplicationEvent {

    private final String username;
    private final String password;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param username
     * @param password
     */
    public UserAccountPasswordChangedEvent(Object source, String username, String password) {
        this.username = username;
        this.password = password;
    }

}
