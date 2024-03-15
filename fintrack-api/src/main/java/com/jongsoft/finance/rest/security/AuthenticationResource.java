package com.jongsoft.finance.rest.security;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.FintrackAuthenticationProvider;
import com.jongsoft.finance.security.TwoFactorHelper;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.ClientAuthentication;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;
import io.micronaut.security.token.render.AccessRefreshToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Authentication")
@Controller(consumes = MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;
    private final FintrackAuthenticationProvider authenticationProvider;
    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final ApplicationEventPublisher<LoginSuccessfulEvent> eventPublisher;
    private final UserProvider userProvider;
    private final AuthenticationFacade authenticationFacade;
    private final FinTrack application;
    private final ProcessEngine processEngine;

    public AuthenticationResource(AccessRefreshTokenGenerator accessRefreshTokenGenerator, FintrackAuthenticationProvider authenticationProvider, RSASignatureConfiguration rsaSignatureConfiguration, ApplicationEventPublisher<LoginSuccessfulEvent> eventPublisher, UserProvider userProvider, AuthenticationFacade authenticationFacade, FinTrack application, ProcessEngine processEngine) {
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.authenticationProvider = authenticationProvider;
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.eventPublisher = eventPublisher;
        this.userProvider = userProvider;
        this.authenticationFacade = authenticationFacade;
        this.application = application;
        this.processEngine = processEngine;
    }

    @ApiDefaults
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post(value = "/api/security/authenticate")
    @Operation(
            summary = "Authenticate",
            description = "Authenticate against FinTrack to obtain a JWT token",
            operationId = "authenticate"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AccessRefreshToken.class)))
    AccessRefreshToken authenticate(@Valid @Body AuthenticationRequest authenticationRequest) {
        var authorization = authenticationProvider.authenticate(
                authenticationRequest.getUsername(),
                authenticationRequest.getSecret());

        var userDetails = authorization.getAuthentication().get();
        var refresh = UUID.randomUUID().toString();

        eventPublisher.publishEvent(new LoginSuccessfulEvent(userDetails));
        return accessRefreshTokenGenerator.generate(refresh, userDetails)
                .map(token -> {
                    application.registerToken(
                            userDetails.getName(),
                            token.getRefreshToken(),
                            token.getExpiresIn());

                    return token;
                })
                .orElseThrow(() -> StatusException.notAuthorized("Unable to generate token"));
    }

    @ApiDefaults
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Put("/api/security/create-account")
    @Operation(
            summary = "Create account",
            description = "Creates a new account",
            operationId = "createAccount"
    )
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(nullable = true)))
    public HttpResponse<Void> createAccount(@Valid @Body AuthenticationRequest authenticationRequest) {
        processEngine.getRuntimeService()
                .startProcessInstanceByKey("RegisterUserAccount", Map.of(
                        "username", authenticationRequest.getIdentity(),
                        "passwordHash", application.getHashingAlgorithm().encrypt(authenticationRequest.getSecret())));

        return HttpResponse.created((Void) null);
    }

    @ApiDefaults
    @Post("/api/security/2-factor")
    @Secured("PRE_VERIFICATION_USER")
    @Operation(
            summary = "Verify MFA token",
            description = "Used to verify the user token against that what is expected. If valid the user will get a new JWT with updated authorizations."
    )
    public AccessRefreshToken mfaValidate(@Valid @Body MultiFactorRequest request) {
        return userProvider.lookup(authenticationFacade.authenticated())
                .filter(user -> TwoFactorHelper.verifySecurityCode(user.getSecret(), request.verificationCode()))
                .map(this::createAccessToken)
                .getOrThrow(() -> StatusException.notAuthorized("Invalid verification code"));
    }

    @ApiDefaults
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/api/security/token-refresh")
    @Operation(
            summary = "Refresh authorization",
            description = "Renew the JWT token if it is about to expire",
            operationId = "refreshToken"
    )
    public AccessRefreshToken refresh(@Body @Valid TokenRefreshRequest request) {
        return userProvider.refreshToken(request.getToken())
                .map(this::createAccessToken)
                .getOrThrow(() -> StatusException.notAuthorized("Invalid refresh token"));
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(value = "/.well-known/public-key")
    @Operation(
            summary = "Get the signing key",
            description = "Use this operation to obtain the public signing key used to sign the JWT."
    )
    public String publicKey() {
        return Base64.encodeBase64String(rsaSignatureConfiguration.getPublicKey().getEncoded());
    }

    private AccessRefreshToken createAccessToken(UserAccount user) {
        var userDetails = new ClientAuthentication(
                user.getUsername(),
                Map.of("rolesKey", Collections.List(user.getRoles()).map(Role::getName).toJava()));
        var refresh = UUID.randomUUID().toString();

        var token = accessRefreshTokenGenerator.generate(refresh, userDetails)
                .orElseThrow(() -> StatusException.notAuthorized("Unable to generate token"));

        application.registerToken(user.getUsername(), token.getRefreshToken(), token.getExpiresIn());
        return token;
    }
}
