package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.domain.commands.*;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.banking.domain.model.ScheduleValue;
import com.jongsoft.finance.core.value.Periodicity;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@DisplayName("Database - Transaction Schedule mutations")
class TransactionScheduleEventListenerIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/transaction/schedule-provider.sql");
    }

    @Test
    @DisplayName("Create new schedule")
    void handleCreated() {
        var schedule = new ScheduleValue(Periodicity.MONTHS, 3);

        CreateScheduleCommand.scheduleCreated("Schedule", schedule, 1L, 2L, 20.2);
    }

    //    @Test
    //    void handleCreateByContract() {
    //        eventPublisher.publishEvent(new CreateScheduleForContractCommand(
    //                "ut_create_by_contract",
    //                new ScheduleValue(Periodicity.MONTHS, 3),
    //                Contract.builder().id(1L).company(Account.builder().id(1L).build()).build(),
    //                Account.builder().id(1L).build(),
    //                500));
    //
    //        var created = entityManager.createQuery(
    //                "from ScheduledTransactionJpa where name = :name",
    // ScheduledTransactionJpa.class)
    //                .setParameter("name", "ut_create_by_contract")
    //                .getSingleResult();
    //
    //        Assertions.assertThat(created).isNotNull();
    //        Assertions.assertThat(created.getContract().getId()).isEqualTo(1L);
    //        Assertions.assertThat(created.getName()).isEqualTo("ut_create_by_contract");
    //        Assertions.assertThat(created.getInterval()).isEqualTo(3);
    //        Assertions.assertThat(created.getPeriodicity()).isEqualTo(Periodicity.MONTHS);
    //    }

    @Test
    @DisplayName("Describe schedule")
    void handleDescribe() {
        DescribeScheduleCommand.scheduleDescribed(2L, "My description", "My name");

        var check = entityManager.find(TransactionScheduleJpa.class, 2L);
        Assertions.assertThat(check.getName()).isEqualTo("My name");
        Assertions.assertThat(check.getDescription()).isEqualTo("My description");
    }

    @Test
    @DisplayName("Set scheduling limits")
    void handleLimit() {
        LimitScheduleCommand.scheduleCreated(
                2L, null, LocalDate.of(2019, 1, 1), LocalDate.of(2022, 1, 1));

        var check = entityManager.find(TransactionScheduleJpa.class, 2L);
        Assertions.assertThat(check.getStart()).isEqualTo(LocalDate.of(2019, 1, 1));
        Assertions.assertThat(check.getEnd()).isEqualTo(LocalDate.of(2022, 1, 1));
    }

    @Test
    @DisplayName("Reschedule schedule")
    void handleReschedule() {
        RescheduleCommand.scheduleRescheduled(2L, null, new ScheduleValue(Periodicity.WEEKS, 3));

        var check = entityManager.find(TransactionScheduleJpa.class, 2L);
        Assertions.assertThat(check.getPeriodicity()).isEqualTo(Periodicity.WEEKS);
        Assertions.assertThat(check.getInterval()).isEqualTo(3);
    }

    @Test
    @DisplayName("Schedule ran")
    void handleScheduleRan() {
        TransactionScheduleRan.scheduleRan(2L, LocalDate.now(), LocalDate.now().plusDays(7));

        var check = entityManager.find(TransactionScheduleJpa.class, 2L);
        Assertions.assertThat(check.getLastRun()).isEqualTo(LocalDate.now());
        Assertions.assertThat(check.getNextRun()).isEqualTo(LocalDate.now().plusDays(7));
    }
}
