package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.lang.collection.Sequence;

import lombok.Getter;

@Getter
public class TransactionTaggingEvent implements ApplicationEvent {

    private final long id;
    private final Sequence<String> tags;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param id
     * @param tags
     */
    public TransactionTaggingEvent(Object source, long id, Sequence<String> tags) {
        this.id = id;
        this.tags = tags;
    }

}
