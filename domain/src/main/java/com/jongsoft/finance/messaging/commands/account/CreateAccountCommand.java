package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;

public record CreateAccountCommand(String name, String currency, String type)
        implements ApplicationEvent {

}
