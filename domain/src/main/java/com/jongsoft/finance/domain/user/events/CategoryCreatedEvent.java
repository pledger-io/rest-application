package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class CategoryCreatedEvent implements ApplicationEvent {

    private final String label;
    private final String description;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param label
     * @param description
     */
    public CategoryCreatedEvent(Object source, String label, String description) {
        this.label = label;
        this.description = description;
    }

}
