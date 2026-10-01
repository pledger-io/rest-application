package com.jongsoft.finance.core.adapter.rest.provider;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

import com.jongsoft.finance.configuration.SecuritySettings;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.commands.RegisterTokenCommand;
import com.jongsoft.finance.core.domain.model.Role;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;

import jakarta.inject.Singleton;

import org.reactivestreams.Publisher;

import java.time.LocalDateTime;

@Singleton
class RefreshTokenHandler implements RefreshTokenPersistence {

    private final UserProvider userProvider;
    private final SecuritySettings securitySettings;

    RefreshTokenHandler(UserProvider userProvider, SecuritySettings securitySettings) {
        this.userProvider = userProvider;
        this.securitySettings = securitySettings;
    }

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        RegisterTokenCommand.tokenRegistered(
                event.getAuthentication().getName(),
                event.getRefreshToken(),
                LocalDateTime.now().plus(securitySettings.getRefreshTokenMaxAge()));
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        return userProvider
                .refreshToken(refreshToken)
                .map(user -> Publishers.just(Authentication.build(
                        user.getUsername().email(),
                        user.getRoles().stream().map(Role::name).toList())))
                .getOrSupply(() -> Publishers.just(new OauthErrorResponseException(
                        INVALID_GRANT, "refresh token not found", null)));
    }
}
