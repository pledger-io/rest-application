package com.jongsoft.finance.rest.profile;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.SessionToken;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.rest.model.SessionResponse;
import com.jongsoft.finance.rest.model.UserProfileResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.TwoFactorHelper;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.UUID;

@Tag(name = "User profile")
@Controller("/api/profile")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProfileResource {

    private final FinTrack application;
    private final CurrentUserProvider currentUserProvider;
    private final UserProvider userProvider;

    @Get
    @Operation(
            operationId = "getProfile",
            summary = "Get profile of authenticated user")
    public Single<UserProfileResponse> get() {
        return Single.create(emitter -> emitter.onSuccess(new UserProfileResponse(currentUserProvider.currentUser())));
    }

    @Patch
    @Operation(
            operationId = "patchProfile",
            summary = "Update part of the user profile",
            description = "This change will be applied to the authenticated user")
    public Single<UserProfileResponse> patch(@Body PatchProfileRequest request) {
        return Single.create(emitter -> {
            var userAccount = currentUserProvider.currentUser();

            if (request.currency() != null) {
                userAccount.changeCurrency(Currency.getInstance(request.currency()));
            }

            if (request.theme() != null) {
                userAccount.changeTheme(request.theme());
            }

            if (request.password() != null) {
                userAccount.changePassword(application.getHashingAlgorithm().encrypt(request.password()));
            }

            emitter.onSuccess(new UserProfileResponse(userAccount));
        });
    }

    @Get(value = "/sessions")
    @Operation(
            operationId = "getActiveSessions",
            summary = "Active Sessions",
            description = "Get a list of active session for the current user."
    )
    Flowable<SessionResponse> sessions() {
        return userProvider.tokens(currentUserProvider.currentUser().getUsername())
                .map(SessionResponse::new);
    }

    @Put(value = "/sessions")
    @Operation(
            operationId = "createToken",
            summary = "Create session token",
            description = "Create a new session token that has a longer validity then default authentication tokens."
    )
    Flowable<SessionResponse> createSession(@Body @Valid TokenCreateRequest request) {
        application.registerToken(
                currentUserProvider.currentUser().getUsername(),
                UUID.randomUUID().toString(),
                (int) ChronoUnit.SECONDS.between(
                        LocalDateTime.now(),
                        request.getExpires().atTime(LocalTime.MIN)));

        return sessions();
    }

    @Delete(value = "/sessions/{id}")
    void deleteSession(@PathVariable long id) {
        userProvider.tokens(currentUserProvider.currentUser().getUsername())
                .filter(token -> token.getId() == id)
                .forEach(SessionToken::revoke);
    }

    @Get(value = "/multi-factor/qr-code", produces = MediaType.IMAGE_PNG)
    @Operation(
            operationId = "getQrCode",
            summary = "QR Code",
            description = "Use this API to obtain a QR code that can be used to scan in a 2-factor application")
    Single<byte[]> qrCode() {
        return Single.create(emitter -> {
            var graphUri = TwoFactorHelper.build2FactorQr(currentUserProvider.currentUser());

            try (var inputStream = new URL(graphUri).openStream()) {
                var reader = new ByteArrayOutputStream();

                int read;
                var byteChunk = new byte[4096];
                while ((read = inputStream.read(byteChunk)) > 0) {
                    reader.write(byteChunk, 0, read);
                }

                emitter.onSuccess(reader.toByteArray());
            }
        });
    }

    @Post("/multi-factor/enable")
    @Operation(
            operationId = "enable2Factor",
            summary = "Enable 2-factor authentication",
            description = "This will activate 2-factor authentication when the security code matches the one recorded")
    void enableMfa(@Body @Valid MultiFactorRequest multiFactorRequest) {
        var userAccount = currentUserProvider.currentUser();
        if (!TwoFactorHelper.verifySecurityCode(userAccount.getSecret(), multiFactorRequest.getVerificationCode())) {
            throw StatusException.badRequest("Invalid verification code provided.");
        }

        userAccount.enableMultiFactorAuthentication();
    }

    @Post("/multi-factor/disable")
    @Operation(
            operationId = "disable2Factor",
            summary = "Disable 2-factor authentication",
            description = "This operation will disable 2-factor authentication, but will only work if it was enabled on the authorized "
                    + "account"
    )
    void disableMfa() {
        currentUserProvider.currentUser().disableMultiFactorAuthentication();
    }

}
