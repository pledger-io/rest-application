package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;

public record RenameAccountCommand(long id, String type, String name, String description, String currency)
        implements ApplicationEvent {
}
