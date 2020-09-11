package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class ScheduledTransactionDescribeEvent implements ApplicationEvent {

    private final long id;
    private final String description;
    private final String name;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param id
     * @param description
     * @param name
     */
    public ScheduledTransactionDescribeEvent(Object source, long id, String description, String name) {
        this.id = id;
        this.description = description;
        this.name = name;
    }

}
