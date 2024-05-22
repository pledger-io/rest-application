package com.jongsoft.finance.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.generator.AccessTokenConfiguration;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CustomRefreshTokenPersistence implements RefreshTokenPersistence {
    private final Logger logger = LoggerFactory.getLogger(CustomRefreshTokenPersistence.class);

    private final UserProvider userProvider;
    private final FinTrack application;
    private final AccessTokenConfiguration accessTokenConfiguration;

    @Inject
    public CustomRefreshTokenPersistence(UserProvider userProvider, FinTrack application, AccessTokenConfiguration accessTokenConfiguration) {
        this.userProvider = userProvider;
        this.application = application;
        this.accessTokenConfiguration = accessTokenConfiguration;
    }

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        logger.debug("Persisting refresh token for user: {}", event.getAuthentication().getName());

        application.registerToken(
                event.getAuthentication().getName(),
                event.getRefreshToken(),
                accessTokenConfiguration.getExpiration());
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        logger.debug("Retrieving authentication for refresh token: {}", refreshToken);

        return userProvider.refreshToken(refreshToken)
                .map(account -> {
                    var roles = account.getRoles().stream().map(Role::getName).toList();
                    return Authentication.build(account.getUsername(), roles);
                })
                .map(Publishers::just)
                .getOrSupply(() -> Publishers.just(
                        new OauthErrorResponseException(IssuingAnAccessTokenErrorCode.INVALID_GRANT, "Invalid refresh token", null)));
    }
}
