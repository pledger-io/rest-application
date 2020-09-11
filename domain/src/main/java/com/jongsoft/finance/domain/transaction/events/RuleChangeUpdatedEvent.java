package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.RuleColumn;

import lombok.Getter;

@Getter
public class RuleChangeUpdatedEvent implements ApplicationEvent {

    private final long ruleChangeId;
    private final RuleColumn column;
    private final String change;

    public RuleChangeUpdatedEvent(Object source, long ruleChangeId, RuleColumn column, String change) {
        this.ruleChangeId = ruleChangeId;
        this.column = column;
        this.change = change;
    }

}
