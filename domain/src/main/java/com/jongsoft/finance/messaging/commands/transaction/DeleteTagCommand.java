package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record DeleteTagCommand(String tag) implements ApplicationEvent {

  public static void tagDeleted(String tag) {
    new DeleteTagCommand(tag).publish();
  }
}
