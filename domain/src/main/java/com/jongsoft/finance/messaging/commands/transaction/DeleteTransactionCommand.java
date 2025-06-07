package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DeleteTransactionCommand(long id) implements ApplicationEvent {

  public static void transactionDeleted(long id) {
    new DeleteTransactionCommand(id).publish();
  }
}
