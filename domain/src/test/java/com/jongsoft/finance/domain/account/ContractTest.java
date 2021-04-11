package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.contract.AttachFileToContractCommand;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import com.jongsoft.finance.messaging.commands.contract.TerminateContract;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTest {

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void construct_StartAfterEnd() {
        LocalDate start = LocalDate.of(2009, 2, 1);
        LocalDate end = LocalDate.of(2009, 1, 1);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Contract(null, "Sample", "", start, end));

        assertThat(exception.getMessage()).isEqualTo("Start cannot be after end of contract.");
    }

    @Test
    void warnBeforeExpires_notPersisted() {
        LocalDate start = LocalDate.of(2009, 1, 1);
        LocalDate end = LocalDate.of(2010, 1, 1);

        final Contract contract = new Contract(null, "Sample", "", start, end);
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                contract::warnBeforeExpires);

        assertThat(exception.getMessage()).isEqualTo("Cannot activate contract warning if contract is not yet persisted.");
    }

    @Test
    void warnBeforeExpires_expiredContract() {
        LocalDate start = LocalDate.now().minusYears(2);
        LocalDate end = start.plusYears(1);

        var contract = Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build();

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                contract::warnBeforeExpires);

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(WarnBeforeExpiryCommand.class);

        assertThat(exception.getMessage()).isEqualTo("Cannot activate contract warning if contract has expired.");
    }

    @Test
    void warnBeforeExpires() {
        ArgumentCaptor<WarnBeforeExpiryCommand> changeCaptor = ArgumentCaptor.forClass(WarnBeforeExpiryCommand.class);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build().warnBeforeExpires();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().id()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().endDate()).isEqualTo(end);
    }

    @Test
    void change() {
        ArgumentCaptor<ChangeContractCommand> changeCaptor = ArgumentCaptor.forClass(ChangeContractCommand.class);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build()
                .change("Updated", "description update", start.plusDays(1), end.plusDays(1));

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().id()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().name()).isEqualTo("Updated");
        assertThat(changeCaptor.getValue().description()).isEqualTo("description update");
        assertThat(changeCaptor.getValue().start()).isEqualTo(start.plusDays(1));
        assertThat(changeCaptor.getValue().end()).isEqualTo(end.plusDays(1));
    }

    @Test
    void change_startAfterEnd() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        var contract = Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build();

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> contract.change("Updated", "", end.plusDays(1), end));

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(ChangeContractCommand.class));

        assertThat(exception.getMessage()).isEqualTo("Start cannot be after end of contract.");
    }

    @Test
    void registerUpload() {
        ArgumentCaptor<AttachFileToContractCommand> changeCaptor = ArgumentCaptor.forClass(AttachFileToContractCommand.class);

        Contract.builder()
                .id(1L)
                .build()
                .registerUpload("1234-dsfasd");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().id()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().fileCode()).isEqualTo("1234-dsfasd");
    }

    @Test
    void terminate() {
        ArgumentCaptor<TerminateContract> changeCaptor = ArgumentCaptor.forClass(TerminateContract.class);

        Contract.builder()
                .id(1L)
                .endDate(LocalDate.of(2010, 1, 1))
                .build()
                .terminate();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().id()).isEqualTo(1L);
    }

    @Test
    void terminate_notExpired() {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> Contract.builder()
                        .id(1L)
                        .endDate(LocalDate.now().plusDays(2))
                        .build()
                        .terminate());

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(TerminateContract.class));

        assertThat(exception.getMessage()).isEqualTo("Contract has not yet expired.");
    }


    @Test
    void terminate_alreadyTerminated() {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> Contract.builder()
                        .id(1L)
                        .endDate(LocalDate.of(2010, 1, 1))
                        .terminated(true)
                        .build()
                        .terminate());

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(TerminateContract.class));

        assertThat(exception.getMessage()).isEqualTo("Contract is already terminated.");
    }

}
