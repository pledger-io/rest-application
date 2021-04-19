package com.jongsoft.finance.domain.importer;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BatchImportTest {

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void delete() {
        var captor = ArgumentCaptor.forClass(DeleteImportJobCommand.class);

        BatchImport.builder()
                .id(1L)
                .build()
                .archive();

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1L);
    }

    @Test
    void delete_alreadyFinished() {
        assertThrows(
                StatusException.class,
                () -> BatchImport.builder()
                        .id(1L)
                        .finished(new Date(2019, 1, 2))
                        .build()
                        .archive(),
                "Cannot archive an import job that has finished running.");
    }

    @Test
    void finish() {
        var captor = ArgumentCaptor.forClass(CompleteImportJobCommand.class);

        BatchImport.builder()
                .id(1L)
                .build()
                .finish(new Date(2019, 1, 1));

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1L);
    }

    @Test
    void finish_AlreadyFinished() {
        assertThrows(
                StatusException.class,
                () -> BatchImport.builder()
                        .id(1L)
                        .finished(new Date(2019, 1, 2))
                        .build()
                        .finish(new Date(2019, 1, 1)),
                "Cannot finish an import which has already completed.");
    }

}
