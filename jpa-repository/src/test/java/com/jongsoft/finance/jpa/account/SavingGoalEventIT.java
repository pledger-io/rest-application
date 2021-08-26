package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.jpa.savings.SavingGoalJpa;
import com.jongsoft.finance.messaging.commands.savings.AdjustSavingGoalCommand;
import com.jongsoft.finance.messaging.commands.savings.AdjustScheduleCommand;
import com.jongsoft.finance.messaging.commands.savings.CompleteSavingGoalCommand;
import com.jongsoft.finance.messaging.commands.savings.CreateSavingGoalCommand;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingGoalEventIT extends JpaTestSetup {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @Inject
    private EntityManager entityManager;

    void setup() {
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/account-provider.sql",
                "sql/account/saving-goal-listener.sql"
        );
    }

    @Test
    void createSavingGoal() {
        setup();

        eventPublisher.publishEvent(new CreateSavingGoalCommand(
                1L,
                "New savings",
                BigDecimal.TEN,
                LocalDate.now().plusDays(10)));

        var check = entityManager.createQuery("select a from SavingGoalJpa a where a.name = :name", SavingGoalJpa.class)
                .setParameter("name", "New savings")
                .getSingleResult();

        Assertions.assertThat(check.getAccount().getIban()).isEqualTo("NLJND200001928233");
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("10");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusDays(10));
    }

    @Test
    void adjustSchedule() {
        setup();

        eventPublisher.publishEvent(new AdjustScheduleCommand(
                1L,
                Schedulable.basicSchedule(
                        1L,
                        LocalDate.now().plusMonths(4),
                        new ScheduleValue(Periodicity.MONTHS, 1))));

        var check = entityManager.find(SavingGoalJpa.class, 1L);

        Assertions.assertThat(check.getAllocated()).isEqualByComparingTo("0");
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("2500.50");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusMonths(4));
        Assertions.assertThat(check.getInterval()).isEqualTo(1);
        Assertions.assertThat(check.getPeriodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    void adjustGoal() {
        setup();

        eventPublisher.publishEvent(new AdjustSavingGoalCommand(
                1L,
                BigDecimal.valueOf(50121.22),
                LocalDate.now().plusYears(100)));

        var check = entityManager.find(SavingGoalJpa.class, 1L);
        Assertions.assertThat(check.getGoal()).isEqualByComparingTo("50121.22");
        Assertions.assertThat(check.getTargetDate()).isEqualTo(LocalDate.now().plusYears(100));
    }

    @Test
    void completeGoal() {
        setup();

        eventPublisher.publishEvent(new CompleteSavingGoalCommand(1L));

        var check = entityManager.find(SavingGoalJpa.class, 1L);
        Assertions.assertThat(check.isArchived()).isTrue();
    }

}
