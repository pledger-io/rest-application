package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.lang.collection.Sequence;

public record SplitTransactionCommand(long id, Sequence<Transaction.Part> split) implements ApplicationEvent {

    public static void transactionSplit(long id, Sequence<Transaction.Part> split) {
        new SplitTransactionCommand(id, split)
                .publish();
    }
}
