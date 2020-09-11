package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class AccountRenamedEvent implements ApplicationEvent {

    private final UserAccount user;
    private final Long accountId;
    private final String type;
    private final String name;
    private final String description;
    private final String currency;

    /**
     * Create a new ApplicationEvent.
     *  @param source the object on which the event initially occurred (never {@code null})
     * @param user
     * @param accountId
     * @param type
     * @param name
     * @param description
     * @param currency
     */
    public AccountRenamedEvent(Object source, UserAccount user, Long accountId,
                               String type, String name, String description, String currency) {
        this.user = user;
        this.accountId = accountId;
        this.type = type;
        this.name = name;
        this.description = description;
        this.currency = currency;
    }

}
