package com.jongsoft.finance.messaging.commands.transaction;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.core.FailureCode;

public record RegisterFailureCommand(long id, FailureCode code) implements ApplicationEvent {
}
