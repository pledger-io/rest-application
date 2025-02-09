package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AuthenticationProvider implements HttpRequestAuthenticationProvider<HttpRequest<?>> {

    private final Logger log = LoggerFactory.getLogger(AuthenticationProvider.class);

    private final FinTrack application;
    private final UserProvider userProvider;

    public AuthenticationProvider(FinTrack application, UserProvider userProvider) {
        this.application = application;
        this.userProvider = userProvider;
    }

    @Override
    public AuthenticationResponse authenticate(HttpRequest<HttpRequest<?>> requestContext, AuthenticationRequest<String, String> authRequest) {
        log.info("Authentication Http request for user {}", authRequest.getIdentity());
        return authenticate(authRequest.getIdentity(), authRequest.getSecret());
    }

    public AuthenticationResponse authenticate(String username, String password) {
        log.debug("Authentication basic request for user {}", username);
        var authenticated = userProvider.lookup(new UserIdentifier(username));
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

            return AuthenticationResponse.success(userAccount.getUsername().email(), roles);
        } else {
            throw AuthenticationResponse.exception(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        }
    }
}
