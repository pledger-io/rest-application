package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.messaging.ApplicationEvent;

import java.io.Serializable;

public record ChangeCurrencyPropertyCommand<T extends Serializable>(
        String code, T value, CurrencyCommandType type) implements ApplicationEvent {

    public static <T extends Serializable> void currencyPropertyChanged(
            String code, T value, CurrencyCommandType type) {
        new ChangeCurrencyPropertyCommand<>(code, value, type).publish();
    }
}
