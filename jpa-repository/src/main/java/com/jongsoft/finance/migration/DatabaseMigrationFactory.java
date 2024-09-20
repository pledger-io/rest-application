package com.jongsoft.finance.migration;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.core.DataSourceMigration;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.data.connection.annotation.Connectable;

@Factory
@Connectable
@RequiresJpa
public class DatabaseMigrationFactory {

    @Context
    public DataSourceMigration datasourceMigration(MigrationDatasourceConfiguration configuration) {
        return new DatasourceMigrationJpa(configuration);
    }

}
