package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

public class CategoryRemovedEvent implements ApplicationEvent {

    private final long id;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param id
     */
    public CategoryRemovedEvent(Object source, long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
