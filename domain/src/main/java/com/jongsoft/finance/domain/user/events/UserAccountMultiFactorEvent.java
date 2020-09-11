package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserAccountMultiFactorEvent implements ApplicationEvent {

    private final String username;
    private final boolean enabled;

    public UserAccountMultiFactorEvent(Object source, String username, boolean enabled) {
        this.username = username;
        this.enabled = enabled;
    }

}
