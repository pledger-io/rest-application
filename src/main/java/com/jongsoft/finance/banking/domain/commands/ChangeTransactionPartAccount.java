package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record ChangeTransactionPartAccount(long id, long accountId) implements ApplicationEvent {

    public static void transactionPartAccountChanged(long id, long accountId) {
        new ChangeTransactionPartAccount(id, accountId).publish();
    }
}
