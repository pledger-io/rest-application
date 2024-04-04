package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.importer.entity.ImportConfig;
import com.jongsoft.finance.messaging.commands.importer.CreateConfigurationCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

class CSVConfigEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/importer/csv-config-provider.sql"
        );
    }

    @Test
    void handleCreatedEvent() {
        eventPublisher.publishEvent(
                new CreateConfigurationCommand("CSVImportProvider", "test-config", "file-code-3"));

        var query = entityManager.createQuery("select c from ImportConfig c where c.name = 'test-config'");
        var check = (ImportConfig) query.getSingleResult();

        Assertions.assertThat(check.getName()).isEqualTo("test-config");
        Assertions.assertThat(check.getFileCode()).isEqualTo("file-code-3");
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
