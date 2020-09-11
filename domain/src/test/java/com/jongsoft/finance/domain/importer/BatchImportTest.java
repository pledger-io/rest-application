package com.jongsoft.finance.domain.importer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.importer.events.BatchImportFinishedEvent;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

class BatchImportTest {

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void finish() {
        ArgumentCaptor<BatchImportFinishedEvent> captor = ArgumentCaptor.forClass(BatchImportFinishedEvent.class);

        BatchImport.builder()
                .id(1L)
                .build()
                .finish(new Date(2019, 1, 1));

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getImportId()).isEqualTo(1L);
    }

    @Test
    void finish_AlreadyFinished() {
        assertThrows(
                IllegalStateException.class,
                () -> BatchImport.builder()
                        .id(1L)
                        .finished(new Date(2019, 1, 2))
                        .build()
                        .finish(new Date(2019, 1, 1)),
                "Cannot finish an import which has already completed.");
    }

}
