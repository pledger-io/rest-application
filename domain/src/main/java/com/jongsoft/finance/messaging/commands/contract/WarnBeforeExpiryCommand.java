package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.core.ApplicationEvent;

import java.time.LocalDate;

public record WarnBeforeExpiryCommand(long id, LocalDate endDate) implements ApplicationEvent {
}
