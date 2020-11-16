package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import lombok.Getter;

@Getter
public class AccountIconAttachedEvent implements ApplicationEvent {

    private final long accountId;
    private final String fileCode;

    public AccountIconAttachedEvent(long accountId, String fileCode) {
        this.accountId = accountId;
        this.fileCode = fileCode;
    }

}
