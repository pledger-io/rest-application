package com.jongsoft.finance.factory;

import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class EventBusFactoryTest {

    @Test
    void eventBus() {
        new EventBusFactory().eventBus(Mockito.mock(ApplicationEventPublisher.class));

        Assertions.assertNotNull(EventBus.getBus());
    }

}