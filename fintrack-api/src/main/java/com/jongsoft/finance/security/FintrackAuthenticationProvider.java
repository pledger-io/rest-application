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
        log.info("Authentication request for user {}", authenticationRequest.getIdentity());
        var authenticated = userProvider.lookup(authenticationRequest.getIdentity().toString());
        if (authenticated.isPresent()) {
            var userAccount = authenticated.get();
            return validateUser(userAccount, authenticationRequest.getSecret().toString());
        } else {
            return Publishers.just(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
        }
    }

    private Publisher<AuthenticationResponse> validateUser(UserAccount userAccount, String secret) {
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

            return Publishers.just(AuthenticationResponse.success(userAccount.getUsername(), roles));
        } else {
            return Publishers.just(AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
        }
    }
}
