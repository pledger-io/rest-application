package com.jongsoft.finance.bpmn.camunda;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties("datasources.default")
public interface CamundaDatasourceConfiguration {

    @Bindable
    @NotBlank
    String getUrl();

    @Bindable
    @NotBlank
    String getUsername();

    @Bindable
    @NotNull
    String getPassword();

    @Bindable
    @NotBlank
    String getDriverClassName();

    @Bindable(defaultValue = "false")
    @NotBlank
    String getAutoUpdate();

    @Bindable(defaultValue = "auto")
    String getHistoryLevel();
}
