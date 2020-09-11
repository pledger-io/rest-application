package com.jongsoft.finance.security;

import javax.inject.Singleton;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.event.LogoutEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private final static ThreadLocal<String> AUTHENTICATED_USER = new ThreadLocal<>();

    public AuthenticationFacadeImpl() {
    }

    @EventListener
    void authenticated(LoginSuccessfulEvent event) {
        if (event.getSource() instanceof UserDetails u) {
            log.info("Login processed for {}", u.getUsername());
            AUTHENTICATED_USER.set(u.getUsername());
        }
    }

    @EventListener
    void internalAuthenticated(InternalAuthenticationEvent event) {
        log.debug("Setting internal authentication for {} on thread", event.getUsername());
        AUTHENTICATED_USER.set(event.getUsername());
    }

    @EventListener
    void logout(LogoutEvent event) {
        log.info("Logout processed for {}", event.getSource());
        AUTHENTICATED_USER.remove();
    }

    @Override
    public String authenticated() {
        return AUTHENTICATED_USER.get();
    }

}
