package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.lang.collection.Sequence;

public record TagTransactionCommand(long id, Sequence<String> tags) implements ApplicationEvent {
}
