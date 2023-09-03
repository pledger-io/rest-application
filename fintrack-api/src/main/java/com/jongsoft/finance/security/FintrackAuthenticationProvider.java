package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

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
        return Flux.create(emitter -> {

            var authenticated = userProvider.lookup(authenticationRequest.getIdentity().toString());
            if (authenticated.isPresent()) {
                var userAccount = authenticated.get();
                validateUser(emitter, userAccount, authenticationRequest.getSecret().toString());
            } else {
                emitter.next(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
            }
        });
    }

    private void validateUser(FluxSink<AuthenticationResponse> emitter, UserAccount userAccount, String secret) {
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

            emitter.next(AuthenticationResponse.success(userAccount.getUsername(), roles));
            emitter.complete();
        } else {
            emitter.error(AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
        }
    }
}
