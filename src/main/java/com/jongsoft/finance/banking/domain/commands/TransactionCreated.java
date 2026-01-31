package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record TransactionCreated(long transactionId) implements ApplicationEvent {

    public static void transactionCreated(long transactionId) {
        new TransactionCreated(transactionId).publish();
    }
}
