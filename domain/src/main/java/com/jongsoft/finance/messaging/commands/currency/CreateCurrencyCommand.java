package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateCurrencyCommand(String name, char symbol, String isoCode) implements ApplicationEvent {
}
