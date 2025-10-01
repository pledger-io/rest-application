package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.SessionToken;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Serdeable.Serializable
public class SessionResponse {

    private final SessionToken wrapped;

    public SessionResponse(SessionToken wrapped) {
        this.wrapped = wrapped;
    }

    @Schema(description = "The identifier of the active session", required = true)
    public long getId() {
        return wrapped.getId();
    }

    @Schema(description = "The description of the session")
    public String getDescription() {
        return wrapped.getDescription();
    }

    @Schema(description = "The long lived token of the session", required = true)
    public String getToken() {
        return wrapped.getToken();
    }

    @Schema(description = "The start date of the session", required = true)
    public LocalDateTime getValidFrom() {
        return wrapped.getValidity().from();
    }

    @Schema(description = "The end date of the session", required = true)
    public LocalDateTime getValidUntil() {
        return wrapped.getValidity().until();
    }
}
