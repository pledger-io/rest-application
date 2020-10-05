package com.jongsoft.finance.migration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("datasources.default")
public interface MigrationDatasourceConfiguration {

    @Bindable(defaultValue = "jdbc:h2:mem:fintrack;DB_CLOSE_DELAY=1000")
    @NotBlank
    String getUrl();

    @Bindable(defaultValue = "sa")
    @NotBlank
    String getUsername();

    @Bindable(defaultValue = "")
    @NotNull
    String getPassword();

    @Bindable(defaultValue = "classpath:db/migration")
    @NotNull
    String[] getMigrationLocations();

}
