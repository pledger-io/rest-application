package com.jongsoft.finance.security;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserProvider;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

@Singleton
public class FintrackAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserProvider userProvider;
    private final Logger log;

    public FintrackAuthenticationProvider(final PasswordEncoder passwordEncoder, final UserProvider userProvider) {
        this.passwordEncoder = passwordEncoder;
        this.userProvider = userProvider;
        this.log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            final HttpRequest<?> httpRequest,
            final AuthenticationRequest<?, ?> authenticationRequest) {
        log.info("Authentication request for user {}", authenticationRequest.getIdentity());
        return Flowable.create(emitter -> {

            var authenticated = userProvider.lookup(authenticationRequest.getIdentity().toString());            
            if (authenticated.isPresent()) {
                var userAccount = authenticated.get();

                if (passwordEncoder.matches(userAccount.getPassword(), authenticationRequest.getSecret().toString())) {
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
