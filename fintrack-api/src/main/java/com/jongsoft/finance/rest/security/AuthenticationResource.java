package com.jongsoft.finance.rest.security;

import com.jongsoft.finance.security.PasswordEncoder;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import javax.validation.Valid;
import java.util.Map;

@Tag(name = "Authentication")
@Controller(consumes = MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;
    private final AuthenticationProvider authenticationProvider;
    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final ApplicationEventPublisher eventPublisher;

    private final PasswordEncoder passwordEncoder;
    private final ProcessEngine processEngine;

    public AuthenticationResource(
            final AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            final AuthenticationProvider authenticationProvider,
            final RSASignatureConfiguration rsaSignatureConfiguration,
            final ApplicationEventPublisher eventPublisher,
            final PasswordEncoder passwordEncoder,
            final ProcessEngine processEngine) {
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.authenticationProvider = authenticationProvider;
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
        this.processEngine = processEngine;
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post(value = "/api/security/authenticate")
    public Single<MutableHttpResponse<?>> authenticate(
            HttpRequest<?> request,
            @Valid @Body AuthenticationRequest authenticationRequest) {
        var response = Flowable.fromPublisher(
                authenticationProvider.authenticate(request, authenticationRequest));

        return response.<MutableHttpResponse<?>>map(authenticated -> {
            if (authenticated.isAuthenticated() && authenticated.getUserDetails().isPresent()) {
                var userDetails = authenticated.getUserDetails().get();

                eventPublisher.publishEvent(new LoginSuccessfulEvent(userDetails));
                return HttpResponse.ok(accessRefreshTokenGenerator.generate(userDetails).get());
            } else {
                return HttpResponse.unauthorized();
            }
        }).first(HttpResponse.unauthorized());
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Put("/api/security/create-account")
    public void createAccount(@Valid @Body AuthenticationRequest authenticationRequest) {
        processEngine.getRuntimeService()
                .startProcessInstanceByKey("RegisterUserAccount", Map.of(
                        "username", authenticationRequest.getIdentity(),
                        "passwordHash", passwordEncoder.encrypt(authenticationRequest.getSecret())));
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/api/security/token-refresh")
    public Single<MutableHttpResponse<?>> refresh() {
        return Single.create(emitter -> {
            var token = accessRefreshTokenGenerator.generate(null);
            if (token.isEmpty()) {
                emitter.onSuccess(HttpResponse.unauthorized());
            } else {
                emitter.onSuccess(HttpResponse.ok(token.get()));
            }
        });
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(value = "/.well-known/public-key")
    public String publicKey() {
        return Base64.encodeBase64String(rsaSignatureConfiguration.getPublicKey().getEncoded());
    }

}
