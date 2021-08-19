package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FintrackAuthenticationProvider implements AuthenticationProvider {

    private final FinTrack application;
    private final UserProvider userProvider;

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            final HttpRequest<?> httpRequest,
            final AuthenticationRequest<?, ?> authenticationRequest) {
        log.info("Authentication request for user {}", authenticationRequest.getIdentity());
        return Flux.create(emitter -> {

            var authenticated = userProvider.lookup(
                    authenticationRequest.getIdentity().toString());
            if (authenticated.isPresent()) {
                var userAccount = authenticated.get();

                boolean matches = application.getHashingAlgorithm().matches(
                        userAccount.getPassword(),
                        authenticationRequest.getSecret().toString());
                if (matches) {
                    List<String> roles = new ArrayList<>();
                    if (userAccount.isTwoFactorEnabled()) {
                        roles.add("PRE_VERIFICATION_USER");
                    } else {
                        userAccount.getRoles().map(Role::getName).forEach(roles::add);
                    }
                    emitter.next((AuthenticationResponse) () ->
                            Optional.of(new ServerAuthentication(userAccount.getUsername(), roles, Map.of())));
                    emitter.complete();
                } else {
                    emitter.next(new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
                }
            } else {
                emitter.next(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
            }
        });
    }
}
