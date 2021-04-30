package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;

public record ChangeAccountCommand(long id, String iban, String bic, String number)
        implements ApplicationEvent {
}
