package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.lang.collection.List;

import lombok.Getter;

@Getter
public class TransactionSplitEvent implements ApplicationEvent {

    private final long transactionId;
    private final List<Transaction.Part> transactionParts;

    /**
     * Create a new {@code ApplicationEvent}.
     *  @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param transactionId
     * @param transactionParts
     */
    public TransactionSplitEvent(Object source, long transactionId, List<Transaction.Part> transactionParts) {
        this.transactionId = transactionId;
        this.transactionParts = transactionParts;
    }

}
