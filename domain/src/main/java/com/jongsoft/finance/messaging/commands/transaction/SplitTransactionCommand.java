package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.lang.collection.Sequence;

public record SplitTransactionCommand(long id, Sequence<Transaction.Part> split) implements ApplicationEvent {
}
