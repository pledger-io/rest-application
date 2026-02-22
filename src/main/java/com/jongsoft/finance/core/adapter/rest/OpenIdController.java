package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.configuration.OpenIdConfiguration;
import com.jongsoft.finance.rest.OpenIdApi;
import com.jongsoft.finance.rest.model.OpenIdConfiguration200Response;

import io.micronaut.http.annotation.Controller;

@Controller
class OpenIdController implements OpenIdApi {

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
