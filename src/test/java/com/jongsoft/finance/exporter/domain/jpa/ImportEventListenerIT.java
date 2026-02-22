package com.jongsoft.finance.exporter.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.exporter.domain.commands.CompleteImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.CreateImportJobCommand;
import com.jongsoft.finance.exporter.domain.commands.DeleteImportJobCommand;
import com.jongsoft.finance.exporter.domain.jpa.entity.ImportJpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Batch Import changes")
class ImportEventListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql",
                "sql/importer/import-provider.sql");
    }

    @Test
    @DisplayName("Create new import job")
    void handleCreatedEvent() {
        CreateImportJobCommand.importJobCreated(1L, "batch-slug", "file-code-5");

        var query =
                entityManager.createQuery("select c from ImportJpa c where c.slug = 'batch-slug'");
        var check = (ImportJpa) query.getSingleResult();

        Assertions.assertThat(check.getSlug()).isEqualTo("batch-slug");
        Assertions.assertThat(check.getFileCode()).isEqualTo("file-code-5");
        Assertions.assertThat(check.getConfig().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Mark import job as finished")
    void handleFinishedEvent() {
        CompleteImportJobCommand.importJobCompleted(1L);

        var check = entityManager.find(ImportJpa.class, 1L);
        Assertions.assertThat(check.getFinished()).isNotNull();
    }

    @Test
    @DisplayName("Delete import job")
    void handleDeletedEvent() {
        DeleteImportJobCommand.importJobDeleted(1L);

        var check = entityManager.find(ImportJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }
}
