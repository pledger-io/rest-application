package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;

import lombok.Getter;

@Getter
public class TransactionAccountChangedEvent implements ApplicationEvent {

    private final long transactionPartId;
    private final Account account;

    public TransactionAccountChangedEvent(Object source, long transactionPartId, Account account) {
        this.transactionPartId = transactionPartId;
        this.account = account;
    }

}
