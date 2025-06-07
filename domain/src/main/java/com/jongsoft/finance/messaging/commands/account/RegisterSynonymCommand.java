package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RegisterSynonymCommand(long accountId, String synonym) implements ApplicationEvent {

  public static void synonymRegistered(long accountId, String synonym) {
    new RegisterSynonymCommand(accountId, synonym).publish();
  }
}
