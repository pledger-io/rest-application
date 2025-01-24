package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeTransactionPartAccount(long id, long accountId) implements ApplicationEvent {

    public static void transactionPartAccountChanged(long id, long accountId) {
        new ChangeTransactionPartAccount(id, accountId)
                .publish();
    }
}
