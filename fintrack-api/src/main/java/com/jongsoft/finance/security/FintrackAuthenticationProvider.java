package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FintrackAuthenticationProvider implements AuthenticationProvider<HttpRequest<?>> {

    private final FinTrack application;
    private final UserProvider userProvider;

    public Publisher<AuthenticationResponse> authenticate(
            final HttpRequest<?> httpRequest,
            final AuthenticationRequest<?, ?> authenticationRequest) {
        log.info("Authentication Http request for user {}", authenticationRequest.getIdentity());
        try {
            return Publishers.just(authenticate(
                    authenticationRequest.getIdentity().toString(),
                    authenticationRequest.getSecret().toString()));
        } catch (AuthenticationException exception) {
            return Publishers.just(exception);
        }
    }

    public AuthenticationResponse authenticate(String username, String password) {
        log.info("Authentication basic request for user {}", username);
        var authenticated = userProvider.lookup(username);
        if (authenticated.isPresent()) {
            var userAccount = authenticated.get();
            return validateUser(userAccount, password);
        } else {
            throw AuthenticationResponse.exception(AuthenticationFailureReason.USER_NOT_FOUND);
        }
    }

    private AuthenticationResponse validateUser(UserAccount userAccount, String secret) {
        boolean matches = application.getHashingAlgorithm().matches(userAccount.getPassword(), secret);
        if (matches) {
            List<String> roles = new ArrayList<>();
            if (userAccount.isTwoFactorEnabled()) {
                roles.add("PRE_VERIFICATION_USER");
            } else {
                userAccount.getRoles()
                        .map(Role::getName)
                        .forEach(roles::add);
            }

            return AuthenticationResponse.success(userAccount.getUsername(), roles);
        } else {
            throw AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        }
    }
}
