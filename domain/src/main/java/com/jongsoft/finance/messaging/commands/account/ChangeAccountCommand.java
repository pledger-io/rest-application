package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeAccountCommand(long id, String iban, String bic, String number)
    implements ApplicationEvent {

  /**
   * Method to trigger a change in the account details associated with a given ID.
   *
   * @param id the ID of the account
   * @param iban the new IBAN of the account
   * @param bic the new BIC of the account
   * @param number the new account number
   */
  public static void accountChanged(long id, String iban, String bic, String number) {
    new ChangeAccountCommand(id, iban, bic, number).publish();
  }
}
