package com.jongsoft.finance.exporter.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.exporter.adapter.api.ImportConfigurationProvider;
import com.jongsoft.finance.exporter.domain.commands.CreateConfigurationCommand;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportConfig;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - CSV Importer Configuration")
class CSVConfigProviderJpaIT extends JpaTestSetup {

    @Inject
    private ImportConfigurationProvider csvConfigProvider;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql", "sql/base-setup.sql", "sql/importer/csv-config-provider.sql");
    }

    @Test
    @DisplayName("Lookup all configurations")
    void lookup() {
        Assertions.assertThat(csvConfigProvider.lookup()).hasSize(1).first().satisfies(batch -> {
            Assertions.assertThat(batch.getFileCode()).isEqualTo("file-code-1");
            Assertions.assertThat(batch.getType()).isEqualTo("CSVImportProvider");
        });
    }

    @Test
    @DisplayName("Lookup configuration by name")
    void lookup_name() {
        var check = csvConfigProvider.lookup("sample-config");

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getFileCode()).isEqualTo("file-code-1");
    }

    @Test
    @DisplayName("Lookup configuration by name - incorrect user")
    void lookup_nameIncorrectUser() {
        var check = csvConfigProvider.lookup("other-config");

        Assertions.assertThat(check.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Create new configuration")
    void handleCreatedEvent() {
        CreateConfigurationCommand.configurationCreated(
                "CSVImportProvider", "test-config", "file-code-3");

        var query = entityManager.createQuery(
                "select c from ImportConfig c where c.name = 'test-config'");
        var check = (ImportConfig) query.getSingleResult();

        Assertions.assertThat(check.getName()).isEqualTo("test-config");
        Assertions.assertThat(check.getFileCode()).isEqualTo("file-code-3");
        Assertions.assertThat(check.getType()).isEqualTo("CSVImportProvider");
    }
}
