package com.jongsoft.finance.rest.scheduler;

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
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
        StepVerifier.create(subject.list())
                .assertNext(response -> assertThat(response.getName()).isEqualTo("Monthly gym membership"))
                .verifyComplete();
    }

    @Test
    void create() {
        var request = ScheduledTransactionCreateRequest.builder()
                .amount(22.2)
                .name("Sample schedule")
                .schedule(new ScheduledTransactionCreateRequest.ScheduleValue())
                .destination(ScheduledTransactionCreateRequest.EntityRef.builder().id(1L).build())
                .source(ScheduledTransactionCreateRequest.EntityRef.builder().id(2L).build())
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(Account.builder().id(1L).build()));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(Account.builder().id(2L).build()));
        Mockito.when(transactionScheduleProvider.lookup())
                .thenReturn(Collections.List(scheduledTransaction, ScheduledTransaction.builder()
                        .name("Sample schedule")
                        .build()));

        StepVerifier.create(subject.create(request))
                .assertNext(response -> assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.CREATED.getCode()))
                .verifyComplete();
    }

    @Test
    void get() {
        StepVerifier.create(subject.get(1L))
                .assertNext(response -> {
                    assertThat(response.getName()).isEqualTo("Monthly gym membership");
                    assertThat(response.getDescription()).isEqualTo("Gym membership");
                    assertThat(response.getRange().getStart()).isEqualTo(LocalDate.parse("2019-01-01"));
                    assertThat(response.getRange().getEnd()).isEqualTo(LocalDate.parse("2021-01-01"));
                })
                .verifyComplete();
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

        StepVerifier.create(subject.patch(1L, request))
                .assertNext(response -> {
                    assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());

                })
                .verifyComplete();

        Mockito.verify(scheduledTransaction).describe("New name", "Updated description");
        Mockito.verify(scheduledTransaction).limit(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1));
    }

    @Test
    void remove() {
        var response = subject.remove(1L);

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());

        Mockito.verify(scheduledTransaction).terminate();
    }

    @Test
    void remove_notFound() {
        var response = subject.remove(2L);

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }
}
