package com.jongsoft.finance.security;

import com.jongsoft.finance.messaging.InternalAuthenticationEvent;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuthenticationFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFacade.class);
    private static final ThreadLocal<String> AUTHENTICATED_USER = new ThreadLocal<>();

    /**
     * Get the authenticated username.
     *
     * @return the authenticated username
     */
    public String authenticated() {
        log.trace("[{}] - request authenticated user.", AUTHENTICATED_USER.get());
        return AUTHENTICATED_USER.get();
    }

    @EventListener
    public void internalAuthenticated(InternalAuthenticationEvent event) {
        log.trace("[{}] - Setting internal authentication on thread", event.getUsername());
        AUTHENTICATED_USER.set(event.getUsername());
    }
}
