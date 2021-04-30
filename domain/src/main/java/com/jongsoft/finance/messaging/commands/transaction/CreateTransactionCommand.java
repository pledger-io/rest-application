package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.transaction.Transaction;

public record CreateTransactionCommand(Transaction transaction) implements ApplicationEvent {
}
