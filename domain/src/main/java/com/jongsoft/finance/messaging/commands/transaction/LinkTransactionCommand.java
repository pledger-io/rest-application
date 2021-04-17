package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;

public record LinkTransactionCommand(long id, LinkType type, String relation) implements ApplicationEvent {
    public enum LinkType {
        CATEGORY,
        EXPENSE,
        CONTRACT,
        IMPORT
    }
}
