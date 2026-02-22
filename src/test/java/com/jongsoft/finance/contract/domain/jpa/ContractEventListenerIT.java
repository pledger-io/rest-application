package com.jongsoft.finance.contract.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.contract.domain.commands.*;
import com.jongsoft.finance.contract.domain.jpa.entity.ContractJpa;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Database - Contract mutations")
class ContractEventListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/account/contract-provider.sql");
    }

    @Test
    @DisplayName("Create new contract")
    void handleContractCreated() {
        CreateContractCommand.contractCreated(
                1L,
                "Contract create",
                "MY description",
                LocalDate.of(2019, 1, 22),
                LocalDate.of(2022, 3, 3));
    }

    @Test
    @DisplayName("Update contract")
    void handleContractChanged() {
        ChangeContractCommand.contractChanged(
                1L,
                "Updated name",
                "New description",
                LocalDate.of(2019, 1, 22),
                LocalDate.of(2022, 3, 3));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getName()).isEqualTo("Updated name");
        Assertions.assertThat(check.getDescription()).isEqualTo("New description");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 1, 22));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2022, 3, 3));
    }

    @Test
    @DisplayName("Warn before expiry")
    void handleContractWarning() {
        WarnBeforeExpiryCommand.warnBeforeExpiry(1L, LocalDate.of(2022, 4, 3));

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2022, 4, 3));
        Assertions.assertThat(check.isWarningActive()).isTrue();
        Assertions.assertThat(check.isNotificationSend()).isFalse();

        entityManager.clear();

        ContractWarningSend.warningSent(1L);
        var updated = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(updated.isNotificationSend()).isTrue();
    }

    @Test
    @DisplayName("Upload file")
    void handleContractUpload() {
        AttachFileToContractCommand.attachFileToContract(1L, "file-token");

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.getFileToken()).isEqualTo("file-token");
    }

    @Test
    @DisplayName("Terminate contract")
    void handleContractTerminated() {
        TerminateContractCommand.contractTerminated(1L);

        var check = entityManager.find(ContractJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }
}
