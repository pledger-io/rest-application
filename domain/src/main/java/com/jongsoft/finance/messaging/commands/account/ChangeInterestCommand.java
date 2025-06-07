package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;
import com.jongsoft.finance.schedule.Periodicity;

public record ChangeInterestCommand(long id, double interest, Periodicity periodicity)
    implements ApplicationEvent {

  /**
   * Notifies the system that the interest for a specific entity has changed.
   *
   * @param id the identifier of the entity
   * @param interest the new interest value
   * @param periodicity the periodicity of the interest
   */
  public static void interestChanged(long id, double interest, Periodicity periodicity) {
    new ChangeInterestCommand(id, interest, periodicity).publish();
  }
}
