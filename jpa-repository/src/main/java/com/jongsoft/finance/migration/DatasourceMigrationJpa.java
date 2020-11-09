package com.jongsoft.finance.migration;

import com.jongsoft.finance.core.DataSourceMigration;
import db.migration.V20200429151821__MigrateEncryptedStorage;
import db.migration.V20200430171321__MigrateToEncryptedDatabase;
import db.migration.V20200503171321__MigrateToDecryptDatabase;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

class DatasourceMigrationJpa implements DataSourceMigration {

    private final MigrationDatasourceConfiguration datasourceConfiguration;

    DatasourceMigrationJpa(MigrationDatasourceConfiguration datasourceConfiguration) {
        this.datasourceConfiguration = datasourceConfiguration;

        migrate();
    }

    private void migrate() {
        var config = new FluentConfiguration();
        config.baselineOnMigrate(true)
                .locations(datasourceConfiguration.getMigrationLocations())
                .javaMigrations(
                        new V20200429151821__MigrateEncryptedStorage(),
                        new V20200430171321__MigrateToEncryptedDatabase(),
                        new V20200503171321__MigrateToDecryptDatabase()
                )
                .dataSource(
                        datasourceConfiguration.getUrl(),
                        datasourceConfiguration.getUsername(),
                        datasourceConfiguration.getPassword()
                );

        new Flyway(config).migrate();
    }
}
