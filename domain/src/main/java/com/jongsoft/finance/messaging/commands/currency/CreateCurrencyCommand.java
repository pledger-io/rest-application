package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateCurrencyCommand(String name, char symbol, String isoCode) implements ApplicationEvent {

    public static void currencyCreated(String name, char symbol, String isoCode) {
        new CreateCurrencyCommand(name, symbol, isoCode)
                .publish();
    }
}
