package com.jongsoft.finance.migration;

import com.jongsoft.finance.core.DataSourceMigration;
import db.migration.V20200429151821__MigrateEncryptedStorage;
import db.migration.V20200430171321__MigrateToEncryptedDatabase;
import db.migration.V20200503171321__MigrateToDecryptDatabase;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

@Slf4j
public class DemoModeMigrationJpa implements DataSourceMigration {

    public DemoModeMigrationJpa() {
        log.info("Setting up with demo database configuration.");

        var config = new FluentConfiguration();
        config.baselineOnMigrate(true)
                .locations("classpath:/db/camunda/h2", "classpath:/db/migration", "classpath:/db/sample")
                .dataSource(
                        "jdbc:h2:mem:finance;MODE=MYSQL",
                        "demo-user",
                        "secret")
                .javaMigrations(
                        new V20200429151821__MigrateEncryptedStorage(),
                        new V20200430171321__MigrateToEncryptedDatabase(),
                        new V20200503171321__MigrateToDecryptDatabase()
                );

        new Flyway(config).migrate();
    }

}
