package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.AccountSynonymJpa;
import com.jongsoft.finance.core.value.Periodicity;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Database - Account mutations")
class AccountEventListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset("sql/clean-up.sql", "sql/base-setup.sql", "sql/account/account-listener.sql");
    }

    @Test
    @DisplayName("Create new account")
    void handleAccountCreate() {
        CreateAccountCommand.accountCreated("New account", "USD", "default");

        var query = entityManager.createQuery(
                "select a from AccountJpa a where a.name = 'New account'");
        var check = (AccountJpa) query.getSingleResult();

        Assertions.assertThat(check.getName()).isEqualTo("New account");
        Assertions.assertThat(check.getType().getLabel()).isEqualTo("default");
        Assertions.assertThat(check.getCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Rename account")
    void handleAccountRename() {
        RenameAccountCommand.accountRenamed(
                1L, "default", "Updated name", "With description", "USD");

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.getName()).isEqualTo("Updated name");
        Assertions.assertThat(check.getDescription()).isEqualTo("With description");
        Assertions.assertThat(check.getCurrency().getCode()).isEqualTo("USD");
        Assertions.assertThat(check.getType().getLabel()).isEqualTo("default");
    }

    @Test
    @DisplayName("Change account details")
    void handleAccountChange() {
        ChangeAccountCommand.accountChanged(1L, "NLUPDATED-IBAN", "BIC", "NUMBER");

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.getIban()).isEqualTo("NLUPDATED-IBAN");
        Assertions.assertThat(check.getBic()).isEqualTo("BIC");
        Assertions.assertThat(check.getNumber()).isEqualTo("NUMBER");
    }

    @Test
    @DisplayName("Change interest")
    void handleAccountInterestChange() {
        ChangeInterestCommand.interestChanged(3L, 1.23, Periodicity.MONTHS);

        var check = entityManager.find(AccountJpa.class, 3L);
        Assertions.assertThat(check.getInterest()).isEqualTo(1.23);
        Assertions.assertThat(check.getInterestPeriodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    @DisplayName("Change icon")
    void handleAccountIconEvent() {
        RegisterAccountIconCommand.iconChanged(3L, "file-code", null);

        var check = entityManager.find(AccountJpa.class, 3L);
        Assertions.assertThat(check.getImageFileToken()).isEqualTo("file-code");
    }

    @Test
    @DisplayName("Terminate account")
    void handleAccountTerminate() {
        TerminateAccountCommand.accountTerminated(1L);

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @Test
    @DisplayName("Register synonym")
    void handleRegisterSynonym_update() {
        RegisterSynonymCommand.synonymRegistered(1L, "Test account");

        var query = entityManager.createQuery(
                "select a from AccountSynonymJpa a where a.synonym = 'Test account'");
        var check = (AccountSynonymJpa) query.getSingleResult();
        Assertions.assertThat(check.getAccount().getId()).isEqualTo(1L);
    }
}
