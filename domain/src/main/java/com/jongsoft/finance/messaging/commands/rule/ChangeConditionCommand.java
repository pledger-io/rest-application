package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;

public record ChangeConditionCommand(long id, RuleColumn field, RuleOperation operation, String condition)
        implements ApplicationEvent {
}
