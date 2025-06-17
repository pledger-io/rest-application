package com.jongsoft.finance.rest.security;

import static com.jongsoft.finance.rest.ApiConstants.TAG_SECURITY;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.security.AuthenticationRoles;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.TwoFactorHelper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.handlers.LoginHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;

@Tag(name = TAG_SECURITY)
@Controller(consumes = MediaType.APPLICATION_JSON, value = "/api/security/2-factor")
public class MultiFactorResource {

  private final CurrentUserProvider currentUserProvider;
  private final LoginHandler<HttpRequest<?>, MutableHttpResponse<?>> loginHandler;

  public MultiFactorResource(
      CurrentUserProvider currentUserProvider,
      LoginHandler<HttpRequest<?>, MutableHttpResponse<?>> loginHandler) {
    this.currentUserProvider = currentUserProvider;
    this.loginHandler = loginHandler;
  }

  @Post
  @ApiDefaults
  @Secured(AuthenticationRoles.TWO_FACTOR_NEEDED)
  @Operation(
      summary = "Verify MFA token",
      description =
          "Used to verify the user token against that what is expected. If valid the user will get a new JWT with updated authorizations.")
  HttpResponse<?> validateToken(
      @Valid @Body MultiFactorRequest verification, HttpRequest<?> request) {
    var user = currentUserProvider.currentUser();
    if (!TwoFactorHelper.verifySecurityCode(user.getSecret(), verification.verificationCode())) {
      throw StatusException.forbidden("Invalid verification code");
    }

    var authentication =
        Authentication.build(
            user.getUsername().email(), user.getRoles().stream().map(Role::getName).toList());

    return loginHandler.loginRefresh(authentication, UUID.randomUUID().toString(), request);
  }
}
