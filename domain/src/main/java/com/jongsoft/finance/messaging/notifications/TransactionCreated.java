package com.jongsoft.finance.messaging.notifications;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record TransactionCreated(long transactionId) implements ApplicationEvent {

  public static void transactionCreated(long transactionId) {
    new TransactionCreated(transactionId).publish();
  }
}
