package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.events.TokenRevokeEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.time.Range;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SessionToken implements AggregateBase {

    private Long id;
    private String token;
    private String description;
    private Range<LocalDateTime> validity;

    @Builder
    SessionToken(Long id, String token, String description, Range<LocalDateTime> validity) {
        this.id = id;
        this.token = token;
        this.description = description;
        this.validity = validity;
    }

    @BusinessMethod
    public void revoke() {
        if (!this.validity.until().isAfter(LocalDateTime.now())) {
            throw StatusException.badRequest("Cannot revoke a session token that is already revoked.");
        }

        this.validity = Dates.range(this.validity.from(), LocalDateTime.now());
        EventBus.getBus().send(new TokenRevokeEvent(
                this.token
        ));
    }
}
