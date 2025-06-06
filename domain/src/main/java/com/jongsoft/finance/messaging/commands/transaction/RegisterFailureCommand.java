package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record RegisterFailureCommand(long id, FailureCode code) implements ApplicationEvent {

  public static void registerFailure(long id, FailureCode code) {
    new RegisterFailureCommand(id, code).publish();
  }
}
