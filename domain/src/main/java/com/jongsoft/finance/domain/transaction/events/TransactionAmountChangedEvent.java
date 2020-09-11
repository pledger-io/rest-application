package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionAmountChangedEvent implements ApplicationEvent {

    private final long transactionId;
    private final double amount;
    private final String currency;

    public TransactionAmountChangedEvent(Object source, long transactionId, double amount, String currency) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

}
