package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.core.ApplicationEvent;

public record TerminateContractCommand(long id) implements ApplicationEvent {
}
