package com.jongsoft.finance.core.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.domain.commands.RevokeTokenCommand;
import com.jongsoft.lang.Dates;
import com.jongsoft.lang.time.Range;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDateTime;

@Introspected
public class SessionToken {

    private final Long id;
    private final String token;
    private final String description;
    private Range<LocalDateTime> validity;

    SessionToken(Long id, String token, String description, Range<LocalDateTime> validity) {
        this.id = id;
        this.token = token;
        this.description = description;
        this.validity = validity;
    }

    public void revoke() {
        if (!this.validity.until().isAfter(LocalDateTime.now())) {
            throw StatusException.badRequest(
                    "Cannot revoke a session token that is already revoked.");
        }

        this.validity = Dates.range(this.validity.from(), LocalDateTime.now());
        RevokeTokenCommand.tokenRevoked(token);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getDescription() {
        return description;
    }

    public Range<LocalDateTime> getValidity() {
        return validity;
    }
}
