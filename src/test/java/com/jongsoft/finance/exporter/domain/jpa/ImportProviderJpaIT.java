package com.jongsoft.finance.exporter.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.exporter.adapter.api.ImportProvider;

import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Batch imports")
public class ImportProviderJpaIT extends JpaTestSetup {

    @Inject
    private ImportProvider importProvider;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql",
                "sql/importer/import-provider.sql");
    }

    @Test
    @DisplayName("Lookup by slug")
    void lookup_slug() {
        var check = importProvider.lookup("test-import-1").get();

        Assertions.assertThat(check.getFileCode()).isEqualTo("Large,CSV,file");
    }

    @Test
    @DisplayName("List all imports")
    void lookup() {
        var check = importProvider.lookup(ImportProvider.FilterCommand.unpaged());

        Assertions.assertThat(check.content()).hasSize(1);
    }
}
