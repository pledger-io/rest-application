package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;

public record ReorderRuleGroupCommand(long id, int sort) implements ApplicationEvent {
}
