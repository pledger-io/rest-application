package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.domain.account.events.AccountChangedEvent;
import com.jongsoft.finance.domain.account.events.AccountCreatedEvent;
import com.jongsoft.finance.domain.account.events.AccountRenamedEvent;
import com.jongsoft.finance.domain.account.events.AccountSynonymEvent;
import com.jongsoft.finance.domain.account.events.AccountTerminatedEvent;

public interface AccountListener {

    void handleAccountCreate(AccountCreatedEvent event);
    void handleAccountRename(AccountRenamedEvent event);
    void handleAccountChange(AccountChangedEvent event);
    void handleAccountTerminate(AccountTerminatedEvent event);
    void handleRegisterSynonym(AccountSynonymEvent event);

}
