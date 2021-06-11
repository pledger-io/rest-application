package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

public record DeleteTagCommand(String tag) implements ApplicationEvent {
}
