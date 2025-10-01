package com.jongsoft.finance.messaging;

import io.micronaut.context.event.ApplicationEvent;

public class InternalAuthenticationEvent extends ApplicationEvent {

    private final String username;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @param username
     * @throws IllegalArgumentException if source is null.
     */
    public InternalAuthenticationEvent(final Object source, final String username) {
        super(source);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
