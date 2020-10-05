package com.jongsoft.finance.migration;

import db.migration.V20200429151821__MigrateEncryptedStorage;
import db.migration.V20200430171321__MigrateToEncryptedDatabase;
import db.migration.V20200503171321__MigrateToDecryptDatabase;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

class DatasourceMigration {

    private final MigrationDatasourceConfiguration datasourceConfiguration;

    DatasourceMigration(MigrationDatasourceConfiguration datasourceConfiguration) {
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
