package com.jongsoft.finance.rest;

import com.jongsoft.finance.config.OpenIdConfiguration;
import com.jongsoft.finance.rest.model.OpenIdConfiguration200Response;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;

@Controller
@Requires(env = "openid")
public class OpenIdController implements OpenIdApi {

  private final OpenIdConfiguration openIdConfiguration;

  public OpenIdController(OpenIdConfiguration openIdConfiguration) {
    this.openIdConfiguration = openIdConfiguration;
  }

  @Override
  public OpenIdConfiguration200Response openIdConfiguration() {
    return new OpenIdConfiguration200Response(
        openIdConfiguration.getAuthority(),
        openIdConfiguration.getClientId(),
        openIdConfiguration.getClientSecret());
  }
}
