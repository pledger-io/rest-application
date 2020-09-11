package com.jongsoft.finance.domain.account.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class AccountSynonymEvent implements ApplicationEvent {

    private final UserAccount userAccount;
    private final String synonym;
    private final long accountId;

    /**
     * Create a new {@code ApplicationEvent}.
     *  @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param userAccount
     * @param synonym   the synonym
     * @param accountId the account id to link it to
     */
    public AccountSynonymEvent(Object source, UserAccount userAccount, String synonym, long accountId) {
        this.userAccount = userAccount;
        this.synonym = synonym;
        this.accountId = accountId;
    }

}
