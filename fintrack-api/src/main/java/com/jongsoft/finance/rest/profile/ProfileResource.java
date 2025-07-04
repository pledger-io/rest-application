package com.jongsoft.finance.rest.profile;

import static com.jongsoft.finance.rest.ApiConstants.TAG_SECURITY_USERS;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.rest.model.SessionResponse;
import com.jongsoft.finance.rest.model.UserProfileResponse;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.TwoFactorHelper;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Tag(name = TAG_SECURITY_USERS)
@Controller("/api/profile")
@Secured(AuthenticationRoles.IS_AUTHENTICATED)
public class ProfileResource {

  private final FinTrack application;
  private final CurrentUserProvider currentUserProvider;
  private final UserProvider userProvider;

  public ProfileResource(
      FinTrack application, CurrentUserProvider currentUserProvider, UserProvider userProvider) {
    this.application = application;
    this.currentUserProvider = currentUserProvider;
    this.userProvider = userProvider;
  }

  @Get
  @Operation(operationId = "getProfile", summary = "Get profile of authenticated user")
  public UserProfileResponse get() {
    return new UserProfileResponse(currentUserProvider.currentUser());
  }

  @Patch
  @Operation(
      operationId = "patchProfile",
      summary = "Update part of the user profile",
      description = "This change will be applied to the authenticated user")
  public UserProfileResponse patch(@Body PatchProfileRequest request) {
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

    return new UserProfileResponse(userAccount);
  }

  @Get(value = "/sessions")
  @Operation(
      operationId = "getActiveSessions",
      summary = "Active Sessions",
      description = "Get a list of active session for the current user.")
  List<SessionResponse> sessions() {
    return userProvider
        .tokens(currentUserProvider.currentUser().getUsername())
        .map(SessionResponse::new)
        .toJava();
  }

  @Put(value = "/sessions")
  @Operation(
      operationId = "createToken",
      summary = "Create session token",
      description = "Create a new session token that has a longer validity then default"
          + " authentication tokens.")
  List<SessionResponse> createSession(@Body @Valid TokenCreateRequest request) {
    application.registerToken(
        currentUserProvider.currentUser().getUsername().email(),
        UUID.randomUUID().toString(),
        (int) ChronoUnit.SECONDS.between(
            LocalDateTime.now(), request.expires().atTime(LocalTime.MIN)));

    return sessions();
  }

  @Delete(value = "/sessions/{id}")
  @Status(HttpStatus.NO_CONTENT)
  void deleteSession(@PathVariable long id) {
    userProvider
        .tokens(currentUserProvider.currentUser().getUsername())
        .filter(token -> token.getId() == id)
        .head()
        .revoke();
  }

  @Get(value = "/multi-factor/qr-code", produces = MediaType.IMAGE_PNG)
  @Operation(
      operationId = "getQrCode",
      summary = "QR Code",
      description = "Use this API to obtain a QR code that can be used to scan in a 2-factor"
          + " application")
  byte[] qrCode() {
    var qrCode = TwoFactorHelper.build2FactorQr(currentUserProvider.currentUser());
    try {
      var generator = new ZxingPngQrGenerator();
      generator.setImageSize(150);
      return generator.generate(qrCode);
    } catch (QrGenerationException e) {
      throw StatusException.internalError("Could not successfully generate QR code");
    }
  }

  @Post("/multi-factor/enable")
  @Status(HttpStatus.NO_CONTENT)
  @Operation(
      operationId = "enable2Factor",
      summary = "Enable 2-factor authentication",
      description = "This will activate 2-factor authentication when the security code matches the"
          + " one recorded")
  void enableMfa(@Body @Valid MultiFactorRequest multiFactorRequest) {
    var userAccount = currentUserProvider.currentUser();
    if (!TwoFactorHelper.verifySecurityCode(
        userAccount.getSecret(), multiFactorRequest.verificationCode())) {
      throw StatusException.badRequest("Invalid verification code provided.");
    }

    userAccount.enableMultiFactorAuthentication();
  }

  @Post("/multi-factor/disable")
  @Status(HttpStatus.NO_CONTENT)
  @Operation(
      operationId = "disable2Factor",
      summary = "Disable 2-factor authentication",
      description = "This operation will disable 2-factor authentication, but will only work if it"
          + " was enabled on the authorized account")
  void disableMfa() {
    currentUserProvider.currentUser().disableMultiFactorAuthentication();
  }
}
