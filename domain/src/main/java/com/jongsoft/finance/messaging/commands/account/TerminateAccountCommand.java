package com.jongsoft.finance.messaging.commands.account;

import com.jongsoft.finance.core.ApplicationEvent;

public record TerminateAccountCommand(long id) implements ApplicationEvent {

}
