package com.jongsoft.finance.domain.importer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.importer.events.BatchImportCreatedEvent;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

class BatchImportConfigTest {

    private ApplicationEventPublisher applicationEventPublisher;

    private BatchImportConfig subject;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        subject = BatchImportConfig.builder()
                .id(1L)
                .user(UserAccount.builder()
                        .id(1L)
                        .username("demo-user")
                        .build())
                .name("demo-config")
                .build();
    }

    @Test
    void createImport() {
        ArgumentCaptor<BatchImportCreatedEvent> changeCaptor = ArgumentCaptor.forClass(BatchImportCreatedEvent.class);

        subject.createImport("sample content");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue().getConfig()).isEqualTo(subject);
        assertThat(changeCaptor.getValue().getUser()).isEqualTo(subject.getUser());
    }
}
