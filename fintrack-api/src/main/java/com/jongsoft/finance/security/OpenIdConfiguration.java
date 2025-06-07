package com.jongsoft.finance.security;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.serde.annotation.Serdeable;

@Requires(env = "openid")
@Serdeable.Serializable
@ConfigurationProperties("application.openid")
public class OpenIdConfiguration {

    private String authority;
    private String clientId;
    private String clientSecret;

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
