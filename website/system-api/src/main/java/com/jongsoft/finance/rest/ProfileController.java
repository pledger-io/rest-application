package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.rest.model.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.TwoFactorHelper;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ProfileController implements ProfileApi {

    private final Logger logger;
    private final UserProvider userProvider;
    private final AuthenticationFacade authenticationFacade;
    private final FinTrack application;

    public ProfileController(
            UserProvider userProvider,
            AuthenticationFacade authenticationFacade,
            FinTrack application) {
        this.userProvider = userProvider;
        this.authenticationFacade = authenticationFacade;
        this.application = application;
        this.logger = LoggerFactory.getLogger(ProfileApi.class);
    }

    @Override
    public HttpResponse<@Valid SessionResponse> createSession(
            String user, SessionRequest sessionRequest) {
        logger.info("Creating long lived session for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot access sessions of another user.");
        }

        var tokenForSession = UUID.randomUUID().toString();
        application.registerToken(user, tokenForSession, (int) ChronoUnit.SECONDS.between(
                LocalDateTime.now(), sessionRequest.getExpires().atTime(LocalTime.MIN)));

        var session = userProvider
                .tokens(new UserIdentifier(user))
                .filter(token -> tokenForSession.equals(token.getToken()))
                .head();

        return HttpResponse.created(convert(session));
    }

    @Override
    public byte @Nullable(inherited = true) [] generateQrCode(String user) {
        logger.info("Retrieving QR-code for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot access sessions of another user.");
        }

        return userProvider
                .lookup(new UserIdentifier(user))
                .map(TwoFactorHelper::build2FactorQr)
                .getOrThrow(
                        () -> StatusException.internalError("Cannot correctly determine user."));
    }

    @Override
    public UserProfileResponse getProfile(String user) {
        logger.info("Retrieving profile for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot access profile of another user.");
        }

        return userProvider
                .lookup(new UserIdentifier(user))
                .map(this::convert)
                .getOrThrow(
                        () -> StatusException.internalError("Cannot correctly determine user."));
    }

    @Override
    public List<@Valid SessionResponse> listSessions(String user) {
        logger.info("Retrieving sessions for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot access sessions of another user.");
        }

        return userProvider.tokens(new UserIdentifier(user)).map(this::convert).toJava();
    }

    @Override
    public HttpResponse<Void> patch2Factor(
            String user, PatchMultiFactorRequest patchMultiFactorRequest) {
        logger.info("Patch 2-factor for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot access 2-factor1 of another user.");
        }

        var userAccount = userProvider
                .lookup(new UserIdentifier(user))
                .getOrThrow(() -> StatusException.notFound("Cannot find user."));
        switch (patchMultiFactorRequest) {
            case EnableMfaRequest e -> {
                if (!TwoFactorHelper.verifySecurityCode(
                        userAccount.getSecret(), e.getVerificationCode())) {
                    throw StatusException.badRequest("Invalid verification code provided.");
                }
                userAccount.enableMultiFactorAuthentication();
            }
            case DisableMfaRequest ignored -> userAccount.disableMultiFactorAuthentication();
            default -> throw StatusException.internalError("Invalid patch multi-factor request.");
        }

        return HttpResponse.noContent();
    }

    @Override
    public UserProfileResponse patchProfile(String user, PatchProfileRequest patchProfileRequest) {
        logger.info("Patching profile for user {}.", user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot patch profile of another user.");
        }

        var userAccount = userProvider
                .lookup(new UserIdentifier(user))
                .getOrThrow(() -> StatusException.notFound("Cannot find user."));

        Optional.ofNullable(patchProfileRequest.getCurrency())
                .map(Currency::getInstance)
                .ifPresent(userAccount::changeCurrency);
        Optional.ofNullable(patchProfileRequest.getTheme()).ifPresent(userAccount::changeTheme);

        var hasher = application.getHashingAlgorithm();
        Optional.ofNullable(patchProfileRequest.getPassword())
                .map(hasher::encrypt)
                .ifPresent(userAccount::changePassword);

        return convert(userAccount);
    }

    @Override
    public HttpResponse<Void> revokeSession(String user, Integer session) {
        logger.info("Revoking session {} for user {}.", session, user);
        if (!user.equalsIgnoreCase(authenticationFacade.authenticated())) {
            throw StatusException.forbidden("Cannot revoke sessions of another user.");
        }

        var sessionToken = userProvider.tokens(new UserIdentifier(user)).stream()
                .filter(token -> token.getId().intValue() == session)
                .findFirst()
                .orElseThrow(() -> StatusException.notFound("Invalid session ID."));

        sessionToken.revoke();
        return HttpResponse.noContent();
    }

    private UserProfileResponse convert(UserAccount userAccount) {
        return new UserProfileResponse(
                userAccount.getTheme(),
                userAccount.getPrimaryCurrency().getCurrencyCode(),
                userAccount.isTwoFactorEnabled());
    }

    private SessionResponse convert(SessionToken sessionToken) {
        var response = new SessionResponse();
        response.setId(sessionToken.getId().intValue());
        response.setDescription(sessionToken.getDescription());
        response.setToken(sessionToken.getToken());
        response.setValid(new DateRange(
                sessionToken.getValidity().from().toLocalDate(),
                sessionToken.getValidity().until().toLocalDate()));
        return response;
    }
}
