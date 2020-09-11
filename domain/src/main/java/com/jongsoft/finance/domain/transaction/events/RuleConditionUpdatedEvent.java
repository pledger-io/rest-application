package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;

import lombok.Getter;

@Getter
public class RuleConditionUpdatedEvent implements ApplicationEvent {

    private final long ruleConditionId;
    private final RuleColumn field;
    private final RuleOperation operation;
    private final String condition;

    public RuleConditionUpdatedEvent(Object source, long ruleConditionId, RuleColumn field, RuleOperation operation, String condition) {
        this.ruleConditionId = ruleConditionId;
        this.field = field;
        this.operation = operation;
        this.condition = condition;
    }

}
