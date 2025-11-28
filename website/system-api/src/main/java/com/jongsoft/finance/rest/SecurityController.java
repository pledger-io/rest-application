package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.commands.StartProcessCommand;
import com.jongsoft.finance.rest.model.TokenRequest;
import com.jongsoft.finance.rest.model.UserRequest;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.TwoFactorHelper;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Controller
public class SecurityController implements SecurityApi {

    private final Logger logger;

    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final FinTrack application;
    private final CurrentUserProvider currentUserProvider;
    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;

    public SecurityController(
            RSASignatureConfiguration rsaSignatureConfiguration,
            FinTrack application,
            CurrentUserProvider currentUserProvider,
            AccessRefreshTokenGenerator accessRefreshTokenGenerator) {
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.application = application;
        this.currentUserProvider = currentUserProvider;
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.logger = LoggerFactory.getLogger(SecurityApi.class);
    }

    @Override
    public HttpResponse<Void> createUser(UserRequest userRequest) {
        StartProcessCommand.startProcess(
                "RegisterUserAccount",
                Map.of(
                        "username",
                        new UserIdentifier(userRequest.getUsername()),
                        "passwordHash",
                        application.getHashingAlgorithm().encrypt(userRequest.getPassword())));

        return HttpResponse.noContent();
    }

    @Override
    public String getJwtSignature() {
        logger.info("Providing the Jwt signature.");
        return Base64.getEncoder()
                .encodeToString(rsaSignatureConfiguration.getPublicKey().getEncoded());
    }

    @Override
    public Object verifyTwoFactor(TokenRequest tokenRequest) {
        var currentUser = currentUserProvider.currentUser();
        var validToken = TwoFactorHelper.verifySecurityCode(
                currentUser.getSecret(), tokenRequest.getVerificationCode());

        if (!validToken) {
            throw StatusException.forbidden("Invalid verification token provided.");
        }

        var authentication = Authentication.build(
                currentUser.getUsername().email(),
                currentUser.getRoles().stream().map(Role::getName).toList());

        return accessRefreshTokenGenerator
                .generate(UUID.randomUUID().toString(), authentication)
                .orElseThrow(() -> StatusException.forbidden("Invalid authentication token."));
    }
}
