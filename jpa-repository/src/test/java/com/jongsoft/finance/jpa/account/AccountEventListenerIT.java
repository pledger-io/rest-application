package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.events.*;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.account.entity.AccountJpa;
import com.jongsoft.finance.jpa.account.entity.AccountSynonymJpa;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;

class AccountEventListenerIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-listener.sql"
        );
    }

    @Test
    void handleAccountCreate() {
        setup();
        eventPublisher.publishEvent(
                new AccountCreatedEvent(
                        this,
                        null,
                        "New account",
                        "USD",
                        "default"));

        var query = entityManager.createQuery("select a from AccountJpa a where a.name = 'New account'");
        var check = (AccountJpa) query.getSingleResult();

        Assertions.assertThat(check.getName()).isEqualTo("New account");
        Assertions.assertThat(check.getType().getLabel()).isEqualTo("default");
        Assertions.assertThat(check.getCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    void handleAccountRename() {
        setup();
        eventPublisher.publishEvent(
                new AccountRenamedEvent(
                        this,
                        null,
                        1L,
                        "default",
                        "Updated name",
                        "With description",
                        "USD"));

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.getName()).isEqualTo("Updated name");
        Assertions.assertThat(check.getDescription()).isEqualTo("With description");
        Assertions.assertThat(check.getCurrency().getCode()).isEqualTo("USD");
        Assertions.assertThat(check.getType().getLabel()).isEqualTo("default");
    }

    @Test
    void handleAccountChange() {
        setup();
        eventPublisher.publishEvent(
                new AccountChangedEvent(
                        this,
                        null,
                        1L,
                        "NLUPDATED-IBAN",
                        "BIC",
                        "NUMBER"));

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.getIban()).isEqualTo("NLUPDATED-IBAN");
        Assertions.assertThat(check.getBic()).isEqualTo("BIC");
        Assertions.assertThat(check.getNumber()).isEqualTo("NUMBER");
    }

    @Test
    void handleAccountInterestChange() {
        setup();
        eventPublisher.publishEvent(
                new AccountInterestEvent(
                        this,
                        3L,
                        1.23,
                        Periodicity.MONTHS));

        var check = entityManager.find(AccountJpa.class, 3L);
        Assertions.assertThat(check.getInterest()).isEqualTo(1.23);
        Assertions.assertThat(check.getInterestPeriodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    void handleAccountIconEvent() {
        setup();
        eventPublisher.publishEvent(new AccountIconAttachedEvent(
                3L,
                "file-code"));

        var check = entityManager.find(AccountJpa.class, 3L);
        Assertions.assertThat(check.getImageFileToken()).isEqualTo("file-code");
    }

    @Test
    void handleAccountTerminate() {
        setup();
        eventPublisher.publishEvent(
                new AccountTerminatedEvent(
                        this,
                        Account.builder().id(1L).build()));

        var check = entityManager.find(AccountJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

    @Test
    void handleRegisterSynonym_update() {
        setup();
        eventPublisher.publishEvent(
                new AccountSynonymEvent(
                        this,
                        null,
                        "Test account",
                        1L));

        var query = entityManager.createQuery("select a from AccountSynonymJpa a where a.synonym = 'Test account'");
        var check = (AccountSynonymJpa) query.getSingleResult();
        Assertions.assertThat(check.getAccount().getId()).isEqualTo(1L);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
