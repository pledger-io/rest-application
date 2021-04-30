package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.RuleColumn;

public record ChangeRuleCommand(long id, RuleColumn column, String change) implements ApplicationEvent {
}
