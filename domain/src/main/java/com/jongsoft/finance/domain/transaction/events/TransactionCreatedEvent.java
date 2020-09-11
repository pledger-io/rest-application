package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.transaction.Transaction;

public class TransactionCreatedEvent implements ApplicationEvent {

    private final Transaction source;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public TransactionCreatedEvent(Transaction source) {
        this.source = source;
    }

    public Transaction getTransaction() {
        return source;
    }

}
