package com.jongsoft.finance.rest.security;

import static com.jongsoft.finance.rest.ApiConstants.TAG_SECURITY;

import com.jongsoft.finance.security.OpenIdConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Requires(env = "openid")
@Tag(name = TAG_SECURITY)
public class OpenIdResource {

  private final OpenIdConfiguration configuration;

  public OpenIdResource(OpenIdConfiguration openIdConfiguration) {
    this.configuration = openIdConfiguration;
  }

  @Secured(SecurityRule.IS_ANONYMOUS)
  @Get(value = "/.well-known/openid-connect")
  @Operation(
      summary = "Get the OpenId Connect",
      description = "Use this operation to get the OpenId connect details.")
  public OpenIdConfiguration openIdConfiguration() {
    return configuration;
  }
}
