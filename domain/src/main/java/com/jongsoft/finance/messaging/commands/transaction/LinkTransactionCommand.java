package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record LinkTransactionCommand(long id, LinkType type, String relation)
    implements ApplicationEvent {
  public enum LinkType {
    CATEGORY,
    EXPENSE,
    CONTRACT,
    IMPORT
  }

  public static void linkCreated(long id, LinkType type, String relation) {
    new LinkTransactionCommand(id, type, relation).publish();
  }
}
