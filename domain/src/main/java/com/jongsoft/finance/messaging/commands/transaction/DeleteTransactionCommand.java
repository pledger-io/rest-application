package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

public record DeleteTransactionCommand(long id) implements ApplicationEvent {
}
