package com.jongsoft.finance.core.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record CreateCurrencyCommand(String name, char symbol, String isoCode)
        implements ApplicationEvent {

    public static void currencyCreated(String name, char symbol, String isoCode) {
        new CreateCurrencyCommand(name, symbol, isoCode).publish();
    }
}
