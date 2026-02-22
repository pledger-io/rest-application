package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record DeleteTransactionCommand(long id) implements ApplicationEvent {

    public static void transactionDeleted(long id) {
        new DeleteTransactionCommand(id).publish();
    }
}
