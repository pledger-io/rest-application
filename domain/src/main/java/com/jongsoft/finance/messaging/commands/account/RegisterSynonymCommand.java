package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;

public record RegisterSynonymCommand(long accountId, String synonym) implements ApplicationEvent {
}
