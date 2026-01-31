package com.jongsoft.finance.core.adapter.rest.provider;

import com.jongsoft.finance.core.adapter.api.Encoder;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.model.Role;
import com.jongsoft.finance.core.value.UserIdentifier;

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
class PledgerAuthenticationProvider implements HttpRequestAuthenticationProvider<HttpRequest<?>> {

    private final Logger logger;
    private final UserProvider userProvider;
    private final Encoder encoder;

    public PledgerAuthenticationProvider(UserProvider userProvider, Encoder encoder) {
        this.userProvider = userProvider;
        this.encoder = encoder;
        this.logger = LoggerFactory.getLogger(PledgerAuthenticationProvider.class);
    }

    @Override
    public @NonNull AuthenticationResponse authenticate(
            @Nullable HttpRequest<HttpRequest<?>> requestContext,
            @NonNull AuthenticationRequest<String, String> authRequest) {
        logger.info("Authentication Http request for user {}.", authRequest.getIdentity());

        var userAccount = userProvider
                .lookup(new UserIdentifier(authRequest.getIdentity()))
                .getOrThrow(() -> AuthenticationResponse.exception(
                        AuthenticationFailureReason.USER_NOT_FOUND));
        boolean matches = encoder.matches(userAccount.getPassword(), authRequest.getSecret());
        if (!matches) {
            throw AuthenticationResponse.exception(
                    AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
        }

        List<String> roles = new ArrayList<>();
        if (userAccount.isTwoFactorEnabled()) {
            roles.add("PRE_VERIFICATION_USER");
        } else {
            userAccount.getRoles().map(Role::name).forEach(roles::add);
        }

        return AuthenticationResponse.success(userAccount.getUsername().email(), roles);
    }
}
