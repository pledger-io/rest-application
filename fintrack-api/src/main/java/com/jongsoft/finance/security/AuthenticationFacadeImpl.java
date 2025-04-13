package com.jongsoft.finance.security;

import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.event.LogoutEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFacadeImpl.class);
    private static final ThreadLocal<String> AUTHENTICATED_USER = new ThreadLocal<>();

    @EventListener
    void authenticated(LoginSuccessfulEvent event) {
        if (event.getSource() instanceof Authentication u) {
            log.info("Login processed for {}", u.getName());
            AUTHENTICATED_USER.set(u.getName());
        }
    }

    @EventListener
    void internalAuthenticated(InternalAuthenticationEvent event) {
        log.trace("[{}] - Setting internal authentication on thread", event.getUsername());
        AUTHENTICATED_USER.set(event.getUsername());
    }

    @EventListener
    void logout(LogoutEvent event) {
        log.info("Logout processed for {}", event.getSource());
        AUTHENTICATED_USER.remove();
    }

    @Override
    public String authenticated() {
        log.trace("[{}] - request authenticated user.", AUTHENTICATED_USER.get());
        return AUTHENTICATED_USER.get();
    }

}
