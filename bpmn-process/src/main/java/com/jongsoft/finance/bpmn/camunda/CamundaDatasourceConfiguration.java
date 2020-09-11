package com.jongsoft.finance.bpmn.camunda;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

@ConfigurationProperties("datasources.default")
public interface CamundaDatasourceConfiguration {

    @Bindable(defaultValue = "jdbc:h2:mem:fintrack;DB_CLOSE_DELAY=1000")
    @NotBlank
    String getUrl();

    @Bindable(defaultValue = "sa")
    @NotBlank
    String getUsername();

    @Bindable(defaultValue = "")
    @NotNull
    String getPassword();

    @Bindable(defaultValue = "org.h2.Driver")
    @NotBlank
    String getDriverClassName();

    @Bindable(defaultValue = "false")
    @NotBlank
    String getAutoUpdate();

}
