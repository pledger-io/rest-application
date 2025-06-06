package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record RenameCurrencyCommand(long id, String name, char symbol, String isoCode)
    implements ApplicationEvent {

  public static void currencyRenamed(long id, String name, char symbol, String isoCode) {
    new RenameCurrencyCommand(id, name, symbol, isoCode).publish();
  }
}
