package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class FintrackAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserProvider userProvider;
    private final Logger log;

    public FintrackAuthenticationProvider(final UserProvider userProvider) {
        this.userProvider = userProvider;
        this.log = LoggerFactory.getLogger(getClass());
        this.passwordEncoder = PasswordEncoder.getInstance();
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
