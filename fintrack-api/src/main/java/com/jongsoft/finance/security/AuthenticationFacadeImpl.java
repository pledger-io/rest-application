package com.jongsoft.finance.security;

import com.jongsoft.finance.bpmn.InternalAuthenticationEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.event.LogoutEvent;
import io.micronaut.security.utils.SecurityService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private final SecurityService securityService;
    private final static ThreadLocal<String> AUTHENTICATED_USER = new ThreadLocal<>();

    public AuthenticationFacadeImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @EventListener
    void authenticated(LoginSuccessfulEvent event) {
        if (event.getSource() instanceof UserDetails u) {
            log.info("Login processed for {}", u.getUsername());
            AUTHENTICATED_USER.set(u.getUsername());
        }
    }

    public void authenticate(String username) {
        log.trace("[{}] - Setting forced authentication on thread", username);
        AUTHENTICATED_USER.set(username);
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
