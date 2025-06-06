package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateAccountCommand(String name, String currency, String type)
    implements ApplicationEvent {

  /**
   * Publishes an account creation event with the specified details.
   *
   * @param name the name of the account owner
   * @param currency the currency associated with the account
   * @param type the type of the account
   */
  public static void accountCreated(String name, String currency, String type) {
    new CreateAccountCommand(name, currency, type).publish();
  }
}
