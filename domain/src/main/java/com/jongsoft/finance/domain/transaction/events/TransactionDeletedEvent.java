package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionDeletedEvent implements ApplicationEvent {

    private final long transactionId;

    public TransactionDeletedEvent(Object source, long transactionId) {
        this.transactionId = transactionId;
    }

}
