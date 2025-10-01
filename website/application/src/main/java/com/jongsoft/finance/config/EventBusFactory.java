package com.jongsoft.finance.config;

import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@Factory
public class EventBusFactory {

    private final Logger logger = LoggerFactory.getLogger(EventBusFactory.class);

    @Context
    public EventBus eventBus(ApplicationEventPublisher<Serializable> eventPublisher) {
        logger.info("Staring the event bus");
        return new EventBus(eventPublisher);
    }
}
