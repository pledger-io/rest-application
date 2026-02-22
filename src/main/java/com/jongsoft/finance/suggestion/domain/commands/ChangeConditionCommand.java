package com.jongsoft.finance.suggestion.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.finance.suggestion.types.RuleOperation;

public record ChangeConditionCommand(
        long id, RuleColumn field, RuleOperation operation, String condition)
        implements ApplicationEvent {

    public static void changeConditionUpdated(
            long id, RuleColumn field, RuleOperation operation, String condition) {
        new ChangeConditionCommand(id, field, operation, condition).publish();
    }
}
