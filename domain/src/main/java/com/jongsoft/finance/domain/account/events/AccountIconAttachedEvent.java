package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.domain.core.events.StorageReplacedEvent;
import lombok.Getter;

@Getter
public class AccountIconAttachedEvent extends StorageReplacedEvent {

    private final long accountId;

    public AccountIconAttachedEvent(long accountId, String fileCode, String oldFileCode) {
        super(fileCode, oldFileCode);
        this.accountId = accountId;
    }

}
