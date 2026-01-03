package com.jongsoft.finance.configuration;

import com.jongsoft.finance.core.domain.ApplicationEvent;
import io.micronaut.context.event.ApplicationEventPublisher;

/**
 * This component wraps the underlying message bus, introducing an abstraction for the eventing
 * system of the application.
 */
public class EventBus {
    private static EventBus INSTANCE;

    private final ApplicationEventPublisher<ApplicationEvent> eventPublisher;

    public EventBus(ApplicationEventPublisher<ApplicationEvent> eventPublisher) {
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

    public static EventBus getBus() {
        return INSTANCE;
    }
}
