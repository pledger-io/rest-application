package com.jongsoft.finance.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;

@ConfigurationProperties("micronaut.application.security")
public interface SecuritySettings {

    @NotNull
    @Bindable(defaultValue = "sample-secret")
    String getSecret();

    @NotNull
    @Bindable(defaultValue = "true")
    boolean isEncrypt();

    @NotNull
    @Bindable(defaultValue = "P30D")
    Duration getRefreshTokenMaxAge();
}
