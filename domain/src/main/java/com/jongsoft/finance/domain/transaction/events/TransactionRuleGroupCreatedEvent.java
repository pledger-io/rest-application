package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionRuleGroupCreatedEvent implements ApplicationEvent {

    private final String name;

    public TransactionRuleGroupCreatedEvent(Object source, String name) {
        this.name = name;
    }

}
