package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.schedule.ScheduledTransactionJpa;
import com.jongsoft.finance.messaging.commands.schedule.*;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;

class TransactionScheduleEventListenerIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    @Inject
    private AuthenticationFacade authenticationFacade;

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/transaction/schedule-provider.sql"
        );
    }

    @Test
    void handleCreated() {
        setup();
        var schedule = new ScheduleValue(Periodicity.MONTHS, 3);

        eventPublisher.publishEvent(new CreateScheduleCommand(
                "Schedule",
                schedule,
                Account.builder().id(1L).build(),
                Account.builder().id(2L).build(),
                20.2));

    }

    @Test
    void handleCreateByContract() {
        setup();
        eventPublisher.publishEvent(new CreateScheduleForContractCommand(
                "ut_create_by_contract",
                new ScheduleValue(Periodicity.MONTHS, 3),
                Contract.builder().id(1L).company(Account.builder().id(1L).build()).build(),
                Account.builder().id(1L).build(),
                500));

        var created = entityManager.createQuery(
                "from ScheduledTransactionJpa where name = :name", ScheduledTransactionJpa.class)
                .setParameter("name", "ut_create_by_contract")
                .getSingleResult();

        Assertions.assertThat(created).isNotNull();
        Assertions.assertThat(created.getContract().getId()).isEqualTo(1L);
        Assertions.assertThat(created.getName()).isEqualTo("ut_create_by_contract");
        Assertions.assertThat(created.getInterval()).isEqualTo(3);
        Assertions.assertThat(created.getPeriodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    void handleDescribe() {
        setup();
        eventPublisher.publishEvent(new DescribeScheduleCommand(
                2L,
                "My description",
                "My name"));

        var check = entityManager.find(ScheduledTransactionJpa.class, 2L);
        Assertions.assertThat(check.getName()).isEqualTo("My name");
        Assertions.assertThat(check.getDescription()).isEqualTo("My description");
    }

    @Test
    void handleLimit() {
        setup();
        eventPublisher.publishEvent(new LimitScheduleCommand(
                2L,
                null,
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2022, 1, 1)));

        var check = entityManager.find(ScheduledTransactionJpa.class, 2L);
        Assertions.assertThat(check.getStart()).isEqualTo(LocalDate.of(2019, 1, 1));
        Assertions.assertThat(check.getEnd()).isEqualTo(LocalDate.of(2022, 1, 1));
    }

    @Test
    void handleReschedule() {
        setup();
        eventPublisher.publishEvent(new RescheduleCommand(
                2L,
                null,
                new ScheduleValue(Periodicity.WEEKS, 3)));

        var check = entityManager.find(ScheduledTransactionJpa.class, 2L);
        Assertions.assertThat(check.getPeriodicity()).isEqualTo(Periodicity.WEEKS);
        Assertions.assertThat(check.getInterval()).isEqualTo(3);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
