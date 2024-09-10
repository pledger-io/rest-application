package com.jongsoft.finance.migration;

import com.jongsoft.finance.core.DataSourceMigration;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Inject;

@Factory
@Connectable
public class DatabaseMigrationFactory {

    @Context
    public DataSourceMigration datasourceMigration(MigrationDatasourceConfiguration configuration) {
        return new DatasourceMigrationJpa(configuration);
    }

}
