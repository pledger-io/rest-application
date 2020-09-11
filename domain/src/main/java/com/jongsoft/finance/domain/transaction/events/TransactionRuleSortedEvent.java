package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionRuleSortedEvent implements ApplicationEvent {

    private final long ruleId;
    private final int sort;

    public TransactionRuleSortedEvent(Object source, long ruleId, int sort) {
        this.ruleId = ruleId;
        this.sort = sort;
    }

}
