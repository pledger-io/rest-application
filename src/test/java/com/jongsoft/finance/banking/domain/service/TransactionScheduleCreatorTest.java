package com.jongsoft.finance.banking.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.ScheduleValue;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.banking.domain.model.TransactionSchedule;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.lang.Collections;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.reflect.ReflectionUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

@Tag("unit")
@DisplayName("Unit - Transaction Schedule Creator")
class TransactionScheduleCreatorTest {

    private TransactionScheduleProvider transactionScheduleProvider;
    private UserProvider userProvider;
    private TransactionCreationHandler transactionCreationHandler;
    private TransactionScheduleCreator subject;

    @BeforeEach
    void setup() {
        transactionScheduleProvider = mock(TransactionScheduleProvider.class);
        userProvider = mock(UserProvider.class);
        transactionCreationHandler = mock(TransactionCreationHandler.class);
        subject = new TransactionScheduleCreator(
                userProvider, transactionScheduleProvider, transactionCreationHandler);

        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Create scheduled transactions")
    void createScheduledTransactions() {
        doReturn(Collections.List(UserAccount.create("account-1@account", "")))
                .when(userProvider)
                .lookup();

        doReturn(Collections.List(create(1L, 100.00, true), create(2L, 50.0, false)))
                .when(transactionScheduleProvider)
                .lookup();

        subject.createScheduledTransactions();

        ArgumentCaptor<CreateTransactionCommand> captor =
                ArgumentCaptor.forClass(CreateTransactionCommand.class);
        verify(transactionCreationHandler, times(1)).handleCreatedEvent(captor.capture());

        var createEvent = captor.getValue();
        assertThat(createEvent.amount().doubleValue()).isEqualTo(100.00d);
        assertThat(createEvent.toAccount()).isEqualTo(2L);
        assertThat(createEvent.fromAccount()).isEqualTo(1L);
        assertThat(createEvent.type()).isEqualTo(TransactionType.CREDIT);
    }

    private TransactionSchedule create(long id, double amount, boolean shouldRun) {
        Account source = mock(Account.class);
        Account destination = mock(Account.class);

        var entity = TransactionSchedule.create(
                "Demo schedule",
                ScheduleValue.of(Periodicity.MONTHS, 1),
                source,
                destination,
                amount);

        doReturn(1L).when(source).getId();
        doReturn(2L).when(destination).getId();

        ReflectionUtils.setField(TransactionSchedule.class, "id", entity, id);
        if (shouldRun) {
            ReflectionUtils.setField(
                    TransactionSchedule.class,
                    "nextRun",
                    entity,
                    LocalDate.now().minusDays(1));
        } else {
            ReflectionUtils.setField(
                    TransactionSchedule.class,
                    "nextRun",
                    entity,
                    LocalDate.now().plusMonths(1));
        }

        return entity;
    }
}
