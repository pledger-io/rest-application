package com.jongsoft.finance.domain.user.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class CategoryRenamedEvent implements ApplicationEvent {

    private final Long categoryId;
    private final String label;
    private final String description;

    /**
     * Create a new ApplicationEvent.
     *  @param source the object on which the event initially occurred (never {@code null})
     * @param categoryId
     * @param label
     * @param description
     */
    public CategoryRenamedEvent(Object source, Long categoryId, String label, String description) {
        this.categoryId = categoryId;
        this.label = label;
        this.description = description;
    }

}
