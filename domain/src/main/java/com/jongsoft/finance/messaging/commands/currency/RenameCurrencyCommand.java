package com.jongsoft.finance.messaging.commands.currency;

import com.jongsoft.finance.core.ApplicationEvent;

public record RenameCurrencyCommand(long id, String name, char symbol, String isoCode) implements ApplicationEvent {
}
