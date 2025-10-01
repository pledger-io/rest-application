package com.jongsoft.finance.security.provider;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
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
public class PledgerAuthenticationProvider
        implements HttpRequestAuthenticationProvider<HttpRequest<?>> {

    private final Logger logger;
    private final UserProvider userProvider;
    private final FinTrack application;

    public PledgerAuthenticationProvider(UserProvider userProvider, FinTrack application) {
        this.userProvider = userProvider;
        this.application = application;
        this.logger = LoggerFactory.getLogger(PledgerAuthenticationProvider.class);
    }

    @Override
    public @NonNull AuthenticationResponse authenticate(
            @Nullable HttpRequest<HttpRequest<?>> requestContext,
            @NonNull AuthenticationRequest<String, String> authRequest) {
        logger.info("Authentication Http request for user {}.", authRequest.getIdentity());

        var userAccount =
                userProvider
                        .lookup(new UserIdentifier(authRequest.getIdentity()))
                        .getOrThrow(
                                () ->
                                        AuthenticationResponse.exception(
                                                AuthenticationFailureReason.USER_NOT_FOUND));
        boolean matches =
                application
                        .getHashingAlgorithm()
                        .matches(userAccount.getPassword(), authRequest.getSecret());
        if (!matches) {
            throw AuthenticationResponse.exception(
                    AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        }

        List<String> roles = new ArrayList<>();
        if (userAccount.isTwoFactorEnabled()) {
            roles.add("PRE_VERIFICATION_USER");
        } else {
            userAccount.getRoles().map(Role::getName).forEach(roles::add);
        }

        return AuthenticationResponse.success(userAccount.getUsername().email(), roles);
    }
}
