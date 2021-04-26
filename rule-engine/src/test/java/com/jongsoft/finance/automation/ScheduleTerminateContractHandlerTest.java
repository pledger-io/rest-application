package com.jongsoft.finance.automation;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.account.TerminateAccountCommand;
import com.jongsoft.finance.messaging.commands.schedule.LimitScheduleCommand;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTerminateContractHandlerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransactionScheduleProvider provider;

    @Mock
    private FilterFactory filterFactory;

    private ScheduleTerminateContractHandler subject;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        new EventBus(eventPublisher);

        subject = new ScheduleTerminateContractHandler(
                provider,
                filterFactory);
    }

    @Test
    void handle_noSchedules() {
        var filterMock = Mockito.mock(
                TransactionScheduleProvider.FilterCommand.class,
                InvocationOnMock::getMock);

        Mockito.doReturn(filterMock)
                .when(filterFactory)
                .schedule();
        Mockito.doReturn(ResultPage.empty())
                .when(provider)
                .lookup(filterMock);

        subject.handle(new TerminateAccountCommand(1L));

        Mockito.verify(filterMock).activeOnly();
        Mockito.verify(filterMock).contract(Collections.List(new EntityRef(1L)));
        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(Mockito.any());
    }

    @Test
    void handle() {
        var filterMock = Mockito.mock(
                TransactionScheduleProvider.FilterCommand.class,
                InvocationOnMock::getMock);
        var schedule = ScheduledTransaction.builder()
                .start(LocalDate.of(2019, 1, 1))
                .id(2L)
                .build();

        Mockito.doReturn(filterMock)
                .when(filterFactory)
                .schedule();
        Mockito.doReturn(ResultPage.of(schedule))
                .when(provider)
                .lookup(filterMock);

        subject.handle(new TerminateAccountCommand(1L));

        var captor = ArgumentCaptor.forClass(LimitScheduleCommand.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        Assertions.assertThat(captor.getValue().id()).isEqualTo(2L);
        Assertions.assertThat(captor.getValue().start()).isEqualTo(LocalDate.of(2019, 1, 1));
        Assertions.assertThat(captor.getValue().end()).isEqualTo(LocalDate.now());
    }

}