package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.account.Account;

import lombok.Getter;

@Getter
public class AccountTerminatedEvent implements ApplicationEvent {

    private final Account account;

    public AccountTerminatedEvent(Object source, Account termiated) {
        account = termiated;
    }

}
