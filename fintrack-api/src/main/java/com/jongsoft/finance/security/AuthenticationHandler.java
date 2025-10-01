package com.jongsoft.finance.security;

import com.jongsoft.finance.messaging.InternalAuthenticationEvent;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.event.LoginSuccessfulEvent;

import jakarta.inject.Singleton;

@Singleton
public class AuthenticationHandler {

    private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

    public AuthenticationHandler(
            ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    void authenticated(LoginSuccessfulEvent event) {
        if (event.getSource() instanceof Authentication u) {
            eventPublisher.publishEvent(
                    new InternalAuthenticationEvent(event.getSource(), u.getName()));
        }
    }
}
