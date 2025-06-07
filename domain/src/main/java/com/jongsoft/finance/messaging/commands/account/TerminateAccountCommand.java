package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record TerminateAccountCommand(long id) implements ApplicationEvent {

  /**
   * Terminates the account identified by the specified ID.
   *
   * @param id the identifier of the account to be terminated
   */
  public static void accountTerminated(long id) {
    new TerminateAccountCommand(id).publish();
  }
}
