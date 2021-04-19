package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseCommand(String name, LocalDate start, BigDecimal budget)
        implements ApplicationEvent {
}
