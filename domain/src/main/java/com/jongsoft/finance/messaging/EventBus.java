package com.jongsoft.finance.messaging;

import io.micronaut.context.event.ApplicationEventPublisher;

import java.io.Serializable;
import java.util.EventObject;

/**
 * This component wraps the underlying message bus, introducing an abstraction for the eventing
 * system of the application.
 */
public class EventBus {
    private static EventBus INSTANCE;

    private final ApplicationEventPublisher<Serializable> eventPublisher;

    public EventBus(ApplicationEventPublisher<Serializable> eventPublisher) {
        INSTANCE = this;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publish an event to the message bus.
     *
     * @param event the event to be published
     */
    public void send(ApplicationEvent event) {
        eventPublisher.publishEvent(event);
    }

    public void sendSystemEvent(EventObject event) {
        eventPublisher.publishEvent(event);
    }

    public static EventBus getBus() {
        return INSTANCE;
    }
}
