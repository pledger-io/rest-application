package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionDescribeEvent implements ApplicationEvent {

    private final long transactionId;
    private final String description;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param transactionId
     * @param description
     */
    public TransactionDescribeEvent(Object source, long transactionId, String description) {
        this.transactionId = transactionId;
        this.description = description;
    }
    
}
