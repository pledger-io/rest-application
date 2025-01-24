package com.jongsoft.finance.messaging.commands.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record ChangeConditionCommand(long id, RuleColumn field, RuleOperation operation, String condition)
        implements ApplicationEvent {

    public static void changeConditionUpdated(long id, RuleColumn field, RuleOperation operation, String condition) {
        new ChangeConditionCommand(id, field, operation, condition)
                .publish();
    }
}
