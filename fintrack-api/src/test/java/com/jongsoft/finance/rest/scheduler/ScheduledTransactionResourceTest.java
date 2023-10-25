package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

class ScheduledTransactionResourceTest extends TestSetup {

    private ScheduledTransactionResource subject;

    private AccountProvider accountProvider;
    private TransactionScheduleProvider transactionScheduleProvider;

    private ScheduledTransaction scheduledTransaction;

    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        transactionScheduleProvider = Mockito.mock(TransactionScheduleProvider.class);
        filterFactory = generateFilterMock();

        subject = new ScheduledTransactionResource(accountProvider, transactionScheduleProvider, filterFactory);

        scheduledTransaction = Mockito.spy(ScheduledTransaction.builder()
                .id(1L)
                .name("Monthly gym membership")
                .amount(22.66)
                .schedule(new ScheduleValue(Periodicity.WEEKS, 4))
                .description("Gym membership")
                .start(LocalDate.of(2019, 1, 1))
                .end(LocalDate.of(2021, 1, 1))
                .source(Account.builder()
                        .id(1L)
                        .type("checking")
                        .name("My account")
                        .currency("EUR")
                        .build())
                .destination(Account.builder().id(2L).type("creditor").currency("EUR").name("Gym").build())
                .build());

        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        Mockito.when(accountProvider.lookup(Mockito.anyLong())).thenReturn(Control.Option());
        Mockito.when(transactionScheduleProvider.lookup()).thenReturn(Collections.List(scheduledTransaction));
    }

    @Test
    void list() {
        Assertions.assertThat(subject.list())
                .isNotNull()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("name", "Monthly gym membership");
    }

    @Test
    void create() {
        var destinationAccount = Account.builder().id(1L).build();
        var sourceAccount = Mockito.spy(Account.builder().id(2L).build());
        var request = ScheduledTransactionCreateRequest.builder()
                .amount(22.2)
                .name("Sample schedule")
                .schedule(new ScheduledTransactionCreateRequest.ScheduleValue(Periodicity.WEEKS, 1))
                .destination(ScheduledTransactionCreateRequest.EntityRef.builder().id(1L).build())
                .source(ScheduledTransactionCreateRequest.EntityRef.builder().id(2L).build())
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(Account.builder().id(1L).build()));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(sourceAccount));
        Mockito.when(transactionScheduleProvider.lookup())
                .thenReturn(Collections.List(scheduledTransaction, ScheduledTransaction.builder()
                        .name("Sample schedule")
                        .build()));

        subject.create(request);

        Mockito.verify(sourceAccount).createSchedule(
                "Sample schedule",
                new ScheduleValue(Periodicity.WEEKS, 1),
                destinationAccount,
                22.2);
    }

    @Test
    void get() {
        Assertions.assertThat(subject.get(1L))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Monthly gym membership")
                .hasFieldOrPropertyWithValue("description", "Gym membership")
                .hasFieldOrPropertyWithValue("range.start", LocalDate.parse("2019-01-01"))
                .hasFieldOrPropertyWithValue("range.end", LocalDate.parse("2021-01-01"));
    }

    @Test
    void patch() {
        var request = ScheduledTransactionPatchRequest.builder()
                .description("Updated description")
                .name("New name")
                .range(new ScheduledTransactionPatchRequest.DateRange(
                        LocalDate.of(2021, 1, 1),
                        LocalDate.of(2022, 1, 1)))
                .build();

        subject.patch(1L, request);

        Mockito.verify(scheduledTransaction).describe("New name", "Updated description");
        Mockito.verify(scheduledTransaction).limit(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1));
    }

    @Test
    void remove() {
        subject.remove(1L);
        Mockito.verify(scheduledTransaction).terminate();
    }

    @Test
    void remove_notFound() {
        Assertions.assertThatThrownBy(() -> subject.remove(2L))
                .isInstanceOf(StatusException.class)
                .hasMessage("No scheduled transaction found with id 2");
    }
}
