package com.jongsoft.finance.domain.transaction;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.schedule.Periodicity;

import io.micronaut.context.event.ApplicationEventPublisher;

class ScheduledTransactionTest {

    private ScheduledTransaction scheduledTransaction;
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        var schedule = new ScheduleValue(Periodicity.MONTHS, 3);
        this.scheduledTransaction = ScheduledTransaction.builder()
                .id(1L)
                .schedule(schedule)
                .source(Account.builder().name("Checking account").type("checking").build())
                .destination(Account.builder().name("TV Corp.").type("creditor").build())
                .amount(20.2)
                .build();
    }

    @Test
    void limit() {
        scheduledTransaction.limit(LocalDate.of(2019, 1, 1), LocalDate.of(2022, 4, 5));

        assertThat(scheduledTransaction.getStart()).isEqualTo(LocalDate.of(2019, 1, 1));
        assertThat(scheduledTransaction.getEnd()).isEqualTo(LocalDate.of(2022, 4, 5));
    }

    @Test
    void limit_endBeforeStart() {
        var exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                scheduledTransaction.limit(LocalDate.of(2022, 4, 5), LocalDate.of(2019, 1, 1)));

        assertThat(exception.getMessage()).isEqualTo("Start of scheduled transaction cannot be after end date.");
    }

    @Test
    void describe() {
        scheduledTransaction.describe("My new name", "My sample description");

        assertThat(scheduledTransaction.getDescription()).isEqualTo("My sample description");
        assertThat(scheduledTransaction.getName()).isEqualTo("My new name");
    }

    @Test
    void adjustSchedule() {
        scheduledTransaction.adjustSchedule(Periodicity.WEEKS, 3);

        assertThat(scheduledTransaction.getSchedule().interval()).isEqualTo(3);
        assertThat(scheduledTransaction.getSchedule().periodicity()).isEqualTo(Periodicity.WEEKS);
    }
}
