package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.savings.AdjustSavingGoalCommand;
import com.jongsoft.finance.messaging.commands.savings.AdjustScheduleCommand;
import com.jongsoft.finance.messaging.commands.savings.CompleteSavingGoalCommand;
import com.jongsoft.finance.messaging.commands.savings.RegisterSavingInstallment;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SavingGoalTest {

    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setup() {
        publisher = Mockito.mock(ApplicationEventPublisher.class);

        new EventBus(publisher);
    }

    @Test
    void computeAllocation_alreadyCompleted() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .allocated(new BigDecimal("321.22"))
                .targetDate(LocalDate.now().plusYears(10))
                .build();

        Assertions.assertThat(savingGoal.computeAllocation()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void computeAllocation() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("200"))
                .allocated(new BigDecimal("25"))
                .targetDate(LocalDate.now().plusYears(1))
                .schedule(new ScheduleValue(Periodicity.MONTHS, 1))
                .build();

        Assertions.assertThat(savingGoal.computeAllocation()).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    @Test
    void adjustGoal() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .targetDate(LocalDate.now().plusYears(10))
                .build();

        savingGoal.adjustGoal(new BigDecimal("7612.22"), LocalDate.now().plusYears(15));

        var captor = ArgumentCaptor.forClass(AdjustSavingGoalCommand.class);
        Mockito.verify(publisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().goal()).isEqualByComparingTo(new BigDecimal("7612.22"));
        Assertions.assertThat(captor.getValue().targetDate()).isEqualTo(LocalDate.now().plusYears(15));
    }

    @Test
    void adjustGoal_inPast() {
        var goal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .targetDate(LocalDate.now().plusYears(10))
                .build();

        var exception = assertThrows(
                StatusException.class,
                () -> goal.adjustGoal(BigDecimal.ONE, LocalDate.now().minusDays(1)));

        Assertions.assertThat(exception.getMessage()).isEqualTo("Target date for a saving goal cannot be in the past.");
    }

    @Test
    void adjustGoal_belowZero() {
        var goal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .targetDate(LocalDate.now().plusYears(10))
                .build();

        var exception = assertThrows(
                StatusException.class,
                () -> goal.adjustGoal(BigDecimal.valueOf(-1), LocalDate.now().plusYears(10)));

        Assertions.assertThat(exception.getMessage()).isEqualTo("The goal cannot be 0 or less.");
    }

    @Test
    void reserveNextPayment() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .schedule(new ScheduleValue(Periodicity.MONTHS, 3))
                .allocated(BigDecimal.valueOf(150.20))
                .targetDate(LocalDate.now().plusYears(2))
                .build();

        savingGoal.reserveNextPayment();

        var captor = ArgumentCaptor.forClass(RegisterSavingInstallment.class);
        Mockito.verify(publisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().id()).isEqualTo(1L);
        Assertions.assertThat(captor.getValue().amount()).isEqualByComparingTo("23.15");
    }

    @Test
    void reserveNextPayment_missingSchedule() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .allocated(BigDecimal.valueOf(150.20))
                .targetDate(LocalDate.now().plusYears(2))
                .build();

        var exception = assertThrows(
                StatusException.class,
                savingGoal::reserveNextPayment);

        Assertions.assertThat(exception)
                .isInstanceOf(StatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", 400)
                .hasMessage("Cannot automatically reserve an installment for saving goal 1. No schedule was setup.");
    }

    @Test
    void completed() {
        SavingGoal.builder()
                .id(1L)
                .goal(new BigDecimal("312.22"))
                .targetDate(LocalDate.now().plusYears(10))
                .build()
                .completed();

        var captor = ArgumentCaptor.forClass(CompleteSavingGoalCommand.class);
        Mockito.verify(publisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().id()).isEqualByComparingTo(1L);
    }

    @Test
    void schedule() {
        SavingGoal.builder()
                .id(1L)
                .targetDate(LocalDate.now().plusYears(10))
                .build()
                .schedule(Periodicity.MONTHS, 1);

        var captor = ArgumentCaptor.forClass(AdjustScheduleCommand.class);
        Mockito.verify(publisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().id()).isEqualByComparingTo(1L);
        Assertions.assertThat(captor.getValue().schedulable().getEnd()).isEqualTo(LocalDate.now().plusYears(10));
        Assertions.assertThat(captor.getValue().schedulable().getSchedule().interval()).isEqualTo(1);
        Assertions.assertThat(captor.getValue().schedulable().getSchedule().periodicity()).isEqualTo(Periodicity.MONTHS);
    }

    @Test
    void schedule_periodicityTooLong() {
        var savingGoal = SavingGoal.builder()
                .id(1L)
                .targetDate(LocalDate.now().plusDays(60))
                .build();

        var exception = assertThrows(
                StatusException.class,
                () -> savingGoal.schedule(Periodicity.MONTHS, 5));

        Assertions.assertThat(exception.getMessage())
                .isEqualTo("Cannot set schedule when first saving would be after the target date of this saving goal.");
    }

}