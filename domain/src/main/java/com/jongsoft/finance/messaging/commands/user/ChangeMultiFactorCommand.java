package com.jongsoft.finance.messaging.commands.user;

import com.jongsoft.finance.core.ApplicationEvent;

public record ChangeMultiFactorCommand(String username, boolean enabled) implements ApplicationEvent {
}
