package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateRuleGroupCommand(String name) implements ApplicationEvent {
}
