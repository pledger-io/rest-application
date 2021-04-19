package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;

public record RenameRuleGroupCommand(long id, String name) implements ApplicationEvent {
}
