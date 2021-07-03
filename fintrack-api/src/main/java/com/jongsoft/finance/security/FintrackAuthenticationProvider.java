package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

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
        return Flowable.create(emitter -> {

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

                    emitter.onNext(new UserDetails(userAccount.getUsername(), roles));
                    emitter.onComplete();
                } else {
                    emitter.onNext(new AuthenticationFailed(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH));
                }
            } else {
                emitter.onNext(new AuthenticationFailed(AuthenticationFailureReason.USER_NOT_FOUND));
            }
        }, BackpressureStrategy.ERROR);
    }
}
