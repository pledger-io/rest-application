package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.importer.events.BatchImportCreatedEvent;
import com.jongsoft.finance.domain.importer.events.BatchImportFinishedEvent;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;

class ImportEventListenerIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        loadDataset(
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql",
                "sql/importer/import-provider.sql"
        );
    }

    @Test
    void handleCreatedEvent() {
        setup();
        eventPublisher.publishEvent(
                new BatchImportCreatedEvent(
                        this,
                        BatchImportConfig.builder()
                                .id(1L)
                                .build(),
                        null,
                        "batch-slug",
                        "file-code-5"));

        var query = entityManager.createQuery("select c from ImportJpa c where c.slug = 'batch-slug'");
        var check = (ImportJpa) query.getSingleResult();

        Assertions.assertThat(check.getSlug()).isEqualTo("batch-slug");
        Assertions.assertThat(check.getFileCode()).isEqualTo("file-code-5");
        Assertions.assertThat(check.getConfig().getId()).isEqualTo(1L);
    }

    @Test
    void handleFinishedEvent() {
        setup();
        eventPublisher.publishEvent(
                new BatchImportFinishedEvent(
                        this,
                        1L));

        var check = entityManager.find(ImportJpa.class, 1L);
        Assertions.assertThat(check.getFinished()).isNotNull();
    }
}
