package com.jongsoft.finance.security;

import javax.validation.constraints.NotNull;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

@ConfigurationProperties("micronaut.application.security")
public interface SecuritySettings {

    @NotNull
    @Bindable(defaultValue = "sample-secret")
    String getSecret();

    @NotNull
    @Bindable(defaultValue = "true")
    boolean isEncrypt();

}
