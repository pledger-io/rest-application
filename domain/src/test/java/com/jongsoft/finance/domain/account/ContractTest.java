package com.jongsoft.finance.domain.account;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.events.ContractChangedEvent;
import com.jongsoft.finance.domain.account.events.ContractTerminatedEvent;
import com.jongsoft.finance.domain.account.events.ContractUploadEvent;
import com.jongsoft.finance.domain.account.events.ContractWarningEvent;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

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

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(ContractWarningEvent.class);

        assertThat(exception.getMessage()).isEqualTo("Cannot activate contract warning if contract has expired.");
    }

    @Test
    void warnBeforeExpires() {
        ArgumentCaptor<ContractWarningEvent> changeCaptor = ArgumentCaptor.forClass(ContractWarningEvent.class);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build().warnBeforeExpires();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().getContractId()).isEqualTo(1L);
    }

    @Test
    void change() {
        ArgumentCaptor<ContractChangedEvent> changeCaptor = ArgumentCaptor.forClass(ContractChangedEvent.class);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);

        Contract.builder()
                .id(1L)
                .endDate(end)
                .startDate(start)
                .build()
                .change("Updated", "description update", start.plusDays(1), end.plusDays(1));

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().getName()).isEqualTo("Updated");
        assertThat(changeCaptor.getValue().getDescription()).isEqualTo("description update");
        assertThat(changeCaptor.getValue().getStart()).isEqualTo(start.plusDays(1));
        assertThat(changeCaptor.getValue().getEnd()).isEqualTo(end.plusDays(1));
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

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(ContractChangedEvent.class));

        assertThat(exception.getMessage()).isEqualTo("Start cannot be after end of contract.");
    }

    @Test
    void registerUpload() {
        ArgumentCaptor<ContractUploadEvent> changeCaptor = ArgumentCaptor.forClass(ContractUploadEvent.class);

        Contract.builder()
                .id(1L)
                .build()
                .registerUpload("1234-dsfasd");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().getStorageToken()).isEqualTo("1234-dsfasd");
    }

    @Test
    void terminate() {
        ArgumentCaptor<ContractTerminatedEvent> changeCaptor = ArgumentCaptor.forClass(ContractTerminatedEvent.class);

        Contract.builder()
                .id(1L)
                .endDate(LocalDate.of(2010, 1, 1))
                .build()
                .terminate();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void terminate_notExpired() {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> Contract.builder()
                        .id(1L)
                        .endDate(LocalDate.now().plusDays(2))
                        .build()
                        .terminate());

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(ContractTerminatedEvent.class));

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

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(ContractTerminatedEvent.class));

        assertThat(exception.getMessage()).isEqualTo("Contract is already terminated.");
    }

}
