package com.jongsoft.finance.banking.domain.jpa;

import com.jongsoft.finance.JpaTestSetup;
import com.jongsoft.finance.banking.domain.commands.AdjustSavingGoalCommand;
import com.jongsoft.finance.banking.domain.commands.AdjustScheduleCommand;
import com.jongsoft.finance.banking.domain.commands.CompleteSavingGoalCommand;
import com.jongsoft.finance.banking.domain.commands.CreateSavingGoalCommand;
import com.jongsoft.finance.banking.domain.jpa.entity.SavingGoalJpa;
import com.jongsoft.finance.banking.domain.model.ScheduleValue;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.Schedulable;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

@DisplayName("Database - Saving Goal mutations")
public class SavingGoalEventIT extends JpaTestSetup {

    @Inject
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        loadDataset(
                "sql/clean-up.sql",
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/account/saving-goal-listener.sql");
    }

    @Test
    @DisplayName("Create new saving goal")
    void createSavingGoal() {
        CreateSavingGoalCommand.savingGoalCreated(
                1L, "New savings", BigDecimal.TEN, LocalDate.now().plusDays(10));

        var check = entityManager
                .createQuery(
                        "select a from SavingGoalJpa a where a.name = :name", SavingGoalJpa.class)
                .setParameter("name", "New savings")
                .getSingleResult();

        Assertions.assertThat(check.getAccount().getIban()).isEqualTo("NLJND200001928233");
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("10");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusDays(10));
    }

    @Test
    @DisplayName("Adjust schedule and goal")
    void adjustSchedule() {
        AdjustScheduleCommand.scheduleAdjusted(
                1L,
                Schedulable.basicSchedule(
                        1L,
                        LocalDate.now().plusMonths(4),
                        new ScheduleValue(Periodicity.MONTHS, 1)));

        var check = entityManager.find(SavingGoalJpa.class, 1L);

        Assertions.assertThat(check.getAllocated()).isEqualByComparingTo("0");
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("2500.50");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusMonths(4));
        Assertions.assertThat(check.getInterval()).isEqualTo(1);
        Assertions.assertThat(check.getPeriodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    @DisplayName("Adjust goal")
    void adjustGoal() {
        AdjustSavingGoalCommand.savingGoalAdjusted(
                1L, BigDecimal.valueOf(50121.22), LocalDate.now().plusYears(100));

        var check = entityManager.find(SavingGoalJpa.class, 1L);
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("50121.22");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusYears(100));
    }

    @Test
    @DisplayName("Mark as completed")
    void completeGoal() {
        CompleteSavingGoalCommand.savingGoalCompleted(1L);

        var check = entityManager.find(SavingGoalJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }
}
