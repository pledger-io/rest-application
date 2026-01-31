package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.banking.domain.model.Transaction;

import java.util.List;

public record SplitTransactionCommand(long id, List<Transaction.Part> split)
        implements ApplicationEvent {

    public static void transactionSplit(long id, List<Transaction.Part> split) {
        new SplitTransactionCommand(id, split).publish();
    }
}
