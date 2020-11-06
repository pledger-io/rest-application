package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TokenRegisteredEvent implements ApplicationEvent {

    private final String username;
    private final String refreshToken;
    private final LocalDateTime expires;

    public TokenRegisteredEvent(String username, String refreshToken, LocalDateTime expires) {
        this.username = username;
        this.refreshToken = refreshToken;
        this.expires = expires;
    }

}
