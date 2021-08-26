package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.messaging.commands.contract.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import java.time.LocalDate;

class ContractEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/contract-provider.sql"
        );
    }

    @Test
    void handleContractCreated() {
        setup();
        eventPublisher.publishEvent(
                new CreateContractCommand(
                        1L,
                        "Contract create",
                        "MY description",
                        LocalDate.of(2019, 1, 22),
                        LocalDate.of(2022, 3, 3)));
    }

    @Test
    void handleContractChanged() {
        setup();
        eventPublisher.publishEvent(
                new ChangeContractCommand(
                        1L,
                        "Updated name",
                        "New description",
                        LocalDate.of(2019, 1, 22),
                        LocalDate.of(2022, 3, 3)));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getName()).isEqualTo("Updated name");
        Assertions.assertThat(check.getDescription()).isEqualTo("New description");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 1, 22));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2022, 3, 3));
    }

    @Test
    void handleContractWarning() {
        setup();
        eventPublisher.publishEvent(
                new WarnBeforeExpiryCommand(
                        1L,
                        LocalDate.of(2022, 4, 3)));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2022, 4, 3));
        Assertions.assertThat(check.isWarningActive()).isTrue();
    }

    @Test
    void handleContractUpload() {
        setup();
        eventPublisher.publishEvent(
                new AttachFileToContractCommand(
                        1L,
                        "file-token"));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getFileToken()).isEqualTo("file-token");
    }

    @Test
    void handleContractTerminated() {
        setup();
        eventPublisher.publishEvent(new TerminateContractCommand(1L));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
