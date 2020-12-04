package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;
import lombok.Getter;

@Getter
public class TokenRevokeEvent implements ApplicationEvent {

    private final String token;

    public TokenRevokeEvent(String token) {
        this.token = token;
    }

}
