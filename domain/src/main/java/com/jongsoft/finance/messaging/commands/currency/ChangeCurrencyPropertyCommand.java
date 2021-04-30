package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.core.ApplicationEvent;

import java.io.Serializable;

public record ChangeCurrencyPropertyCommand<T extends Serializable>(String code, T value, CurrencyCommandType type)
        implements ApplicationEvent {
}
