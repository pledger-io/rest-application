package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class AccountChangedEvent implements ApplicationEvent {

    private final UserAccount user;
    private final Long accountId;
    private final String iban;
    private final String bic;
    private final String number;

    public AccountChangedEvent(Object source, UserAccount user, Long accountId, String iban, String bic, String number) {
        this.accountId = accountId;
        this.user = user;
        this.iban = iban;
        this.bic = bic;
        this.number = number;
    }

}
