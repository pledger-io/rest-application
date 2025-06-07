package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateTransactionCommand(Transaction transaction) implements ApplicationEvent {

  public static void transactionCreated(Transaction transaction) {
    new CreateTransactionCommand(transaction).publish();
  }
}
