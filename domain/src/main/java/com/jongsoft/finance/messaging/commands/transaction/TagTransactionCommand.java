package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.lang.collection.Sequence;

public record TagTransactionCommand(long id, Sequence<String> tags) implements ApplicationEvent {

  public static void tagCreated(long id, Sequence<String> tags) {
    new TagTransactionCommand(id, tags).publish();
  }
}
