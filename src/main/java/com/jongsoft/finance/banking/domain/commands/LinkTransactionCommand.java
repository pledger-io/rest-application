package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.banking.types.TransactionLinkType;

public record LinkTransactionCommand(long id, TransactionLinkType type, Long relationId)
        implements ApplicationEvent {

    public static void linkCreated(long id, TransactionLinkType type, Long relationId) {
        new LinkTransactionCommand(id, type, relationId).publish();
    }
}
