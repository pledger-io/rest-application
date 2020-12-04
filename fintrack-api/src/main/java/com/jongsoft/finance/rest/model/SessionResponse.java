package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.SessionToken;

import java.time.LocalDateTime;

public class SessionResponse {

    private final SessionToken wrapped;

    public SessionResponse(SessionToken wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public String getToken() {
        return wrapped.getToken();
    }

    public LocalDateTime getValidFrom() {
        return wrapped.getValidity().from();
    }

    public LocalDateTime getValidUntil() {
        return wrapped.getValidity().until();
    }
}
