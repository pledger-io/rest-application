package com.jongsoft.finance.core.domain.service;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;

import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class AuthenticationFacadeImpl implements AuthenticationFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFacadeImpl.class);
    private static final ThreadLocal<String> AUTHENTICATED_USER = new ThreadLocal<>();

    public String authenticated() {
        log.trace("[{}] - request authenticated user.", AUTHENTICATED_USER.get());
        return AUTHENTICATED_USER.get();
    }

    @EventListener
    void internalAuthenticated(InternalAuthenticationEvent event) {
        log.trace("[{}] - Setting internal authentication on thread", event.username());
        AUTHENTICATED_USER.set(event.username());
    }
}
