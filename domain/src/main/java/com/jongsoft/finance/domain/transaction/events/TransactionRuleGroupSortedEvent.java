package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionRuleGroupSortedEvent implements ApplicationEvent {

    private final long groupId;
    private final int sortOrder;

    public TransactionRuleGroupSortedEvent(Object source, long groupId, int sortOrder) {
        this.groupId = groupId;
        this.sortOrder = sortOrder;
    }

}
