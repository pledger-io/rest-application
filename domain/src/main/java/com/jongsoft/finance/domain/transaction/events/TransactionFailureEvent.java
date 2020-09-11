package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.FailureCode;

import lombok.Getter;

@Getter
public class TransactionFailureEvent implements ApplicationEvent {

    private final long transactionId;
    private final FailureCode failureCode;

    public TransactionFailureEvent(Object source, long transactionId, FailureCode failureCode) {
        this.transactionId = transactionId;
        this.failureCode = failureCode;
    }

}
