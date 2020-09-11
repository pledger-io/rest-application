package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;

import lombok.Getter;

@Getter
public class TransactionRelationEvent implements ApplicationEvent {
    public enum Type {
        CATEGORY, EXPENSE, CONTRACT, IMPORT
    }

    private final long id;
    private final String relation;
    private final Type type;

    public TransactionRelationEvent(Object source, long id, String relation, Type type) {
        this.id = id;
        this.relation = relation;
        this.type = type;
    }

}
