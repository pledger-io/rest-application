package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class AccountCreatedEvent implements ApplicationEvent {

    private final UserAccount user;
    private final String name;
    private final String currency;
    private final String type;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param user
     * @param name
     * @param currency
     * @param type
     */
    public AccountCreatedEvent(Object source, UserAccount user, String name, String currency, String type) {
        this.user = user;
        this.name = name;
        this.currency = currency;
        this.type = type;
    }

}
