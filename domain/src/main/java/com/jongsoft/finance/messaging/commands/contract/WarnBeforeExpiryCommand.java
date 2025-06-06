package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.LocalDate;

public record WarnBeforeExpiryCommand(long id, LocalDate endDate) implements ApplicationEvent {

  /**
   * Publishes a warning before the expiry date of a certain item identified by the given ID. The
   * warning will be sent as an event through the application's event bus.
   *
   * @param id the unique identifier of the item for which the warning is being sent
   * @param endDate the expiry date of the item to warn about
   */
  public static void warnBeforeExpiry(long id, LocalDate endDate) {
    new WarnBeforeExpiryCommand(id, endDate).publish();
  }
}
