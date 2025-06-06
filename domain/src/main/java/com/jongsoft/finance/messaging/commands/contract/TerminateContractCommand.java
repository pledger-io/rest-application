package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record TerminateContractCommand(long id) implements ApplicationEvent {

  /**
   * Terminates a contract identified by the given ID by creating and publishing a
   * TerminateContractCommand event.
   *
   * @param id the ID of the contract to be terminated
   */
  public static void contractTerminated(long id) {
    new TerminateContractCommand(id).publish();
  }
}
