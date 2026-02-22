package com.jongsoft.finance.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import jakarta.validation.constraints.NotNull;

@ConfigurationProperties("micronaut.application.storage")
public interface StorageSettings {

    @NotNull
    @Bindable
    String getLocation();
}
