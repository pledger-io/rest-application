package com.jongsoft.finance.factory;

import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class EventBusFactory {

    private final Logger log = LoggerFactory.getLogger(EventBusFactory.class);

    @Context
    public EventBus eventBus(ApplicationEventPublisher eventPublisher) {
        log.info("Staring the event bus");
        return new EventBus(eventPublisher);
    }
}
