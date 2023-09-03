package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;
import com.jongsoft.finance.messaging.commands.importer.CreateImportJobCommand;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

class ImportEventListenerIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql",
                "sql/importer/import-provider.sql"
        );
    }

    @Test
    void handleCreatedEvent() {
        eventPublisher.publishEvent(
                new CreateImportJobCommand(
                        1L,
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
        eventPublisher.publishEvent(
                new CompleteImportJobCommand(1L));

        var check = entityManager.find(ImportJpa.class, 1L);
        Assertions.assertThat(check.getFinished()).isNotNull();
    }

    @Test
    void handleDeletedEvent() {
        eventPublisher.publishEvent(new DeleteImportJobCommand(1L));

        var check = entityManager.find(ImportJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
