package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A command to create a new saving goal in the system for the authenticated user.
 */
public record CreateSavingGoalCommand(long accountId, String name, BigDecimal goal, LocalDate targetDate)
        implements ApplicationEvent {
}
