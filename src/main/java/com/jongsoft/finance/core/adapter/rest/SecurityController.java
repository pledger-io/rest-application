package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.Encoder;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.adapter.mail.MailDaemon;
import com.jongsoft.finance.core.domain.model.Role;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.finance.rest.SecurityApi;
import com.jongsoft.finance.rest.model.TokenRequest;
import com.jongsoft.finance.rest.model.UserRequest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

@Controller
class SecurityController implements SecurityApi {

    private final Logger logger;

    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final Encoder encoder;
    private final CurrentUserProvider currentUserProvider;
    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;

    private final UserProvider userProvider;
    private final MailDaemon mailDaemon;

    public SecurityController(
            RSASignatureConfiguration rsaSignatureConfiguration,
            Encoder encoder,
            CurrentUserProvider currentUserProvider,
            AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            UserProvider userProvider,
            MailDaemon mailDaemon) {
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.encoder = encoder;
        this.currentUserProvider = currentUserProvider;
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.userProvider = userProvider;
        this.mailDaemon = mailDaemon;
        this.logger = LoggerFactory.getLogger(SecurityApi.class);
    }

    @Override
    public HttpResponse<Void> createUser(UserRequest userRequest) {
        if (!userProvider.available(new UserIdentifier(userRequest.getUsername()))) {
            throw StatusException.badRequest("Username already taken.");
        }

        UserAccount.create(userRequest.getUsername(), encoder.encrypt(userRequest.getPassword()));

        UserAccount userAccount = userProvider
                .lookup(new UserIdentifier(userRequest.getUsername()))
                .get();
        Properties registrationProperties = new Properties();
        registrationProperties.put("user", userAccount);
        mailDaemon.send(userAccount.getUsername().email(), "registered", registrationProperties);

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
                currentUser.getRoles().stream().map(Role::name).toList());

        return accessRefreshTokenGenerator
                .generate(UUID.randomUUID().toString(), authentication)
                .orElseThrow(() -> StatusException.forbidden("Invalid authentication token."));
    }
}
