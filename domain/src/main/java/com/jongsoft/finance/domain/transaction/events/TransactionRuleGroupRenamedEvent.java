package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionRuleGroupRenamedEvent implements ApplicationEvent {

    private final long ruleGroupId;
    private final String name;

    public TransactionRuleGroupRenamedEvent(Object source, long ruleGroupId, String name) {
        this.ruleGroupId = ruleGroupId;
        this.name = name;
    }

}
