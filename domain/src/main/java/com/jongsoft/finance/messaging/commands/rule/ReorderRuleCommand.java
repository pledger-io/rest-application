package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;

public record ReorderRuleCommand(long id, int sort) implements ApplicationEvent {
}
