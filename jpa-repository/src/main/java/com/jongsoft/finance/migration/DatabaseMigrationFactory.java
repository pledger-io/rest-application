package com.jongsoft.finance.migration;

import com.jongsoft.finance.core.DataSourceMigration;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;

@Factory
public class DatabaseMigrationFactory {

    private final Environment environment;

    @Inject
    public DatabaseMigrationFactory(Environment environment) {
        this.environment = environment;
    }

    @Context
    public DataSourceMigration datasourceMigration(MigrationDatasourceConfiguration configuration) {
        var demoModeEnabled = environment.getActiveNames().contains("demo");

        if (demoModeEnabled) {
            return new DemoModeMigrationJpa();
        }

        return new DatasourceMigrationJpa(configuration);
    }

}
