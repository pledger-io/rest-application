package com.jongsoft.finance.factory;

import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Factory
public class EventBusFactory {

    @Context
    public EventBus eventBus(ApplicationEventPublisher eventPublisher) {
        log.info("Staring the event bus");
        return new EventBus(eventPublisher);
    }

}
