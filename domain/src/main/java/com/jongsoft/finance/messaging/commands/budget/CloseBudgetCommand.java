package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.core.ApplicationEvent;

import java.time.LocalDate;

public record CloseBudgetCommand(long id, LocalDate end) implements ApplicationEvent {
}
