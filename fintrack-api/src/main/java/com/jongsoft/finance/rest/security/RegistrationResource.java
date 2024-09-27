package com.jongsoft.finance.rest.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.rest.ApiDefaults;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import java.util.Map;

@Tag(name = "Authentication")
@Controller(consumes = MediaType.APPLICATION_JSON)
public class RegistrationResource {

    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final FinTrack application;
    private final ProcessEngine processEngine;

    public RegistrationResource(RSASignatureConfiguration rsaSignatureConfiguration, FinTrack application, ProcessEngine processEngine) {
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.application = application;
        this.processEngine = processEngine;
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
    HttpResponse<Void> createAccount(@Valid @Body AuthenticationRequest authenticationRequest) {
        processEngine.getRuntimeService()
                .startProcessInstanceByKey("RegisterUserAccount", Map.of(
                        "username", authenticationRequest.getIdentity(),
                        "passwordHash", application.getHashingAlgorithm().encrypt(authenticationRequest.getSecret())));

        return HttpResponse.created((Void) null);
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
}
