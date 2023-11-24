package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;

public record RuleGroupDeleteCommand(long id) implements ApplicationEvent {
}
