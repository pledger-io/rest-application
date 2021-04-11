package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.core.ApplicationEvent;

public record TerminateContract(long id) implements ApplicationEvent {
}
