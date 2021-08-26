package com.jongsoft.finance.messaging.commands.savings;

import com.jongsoft.finance.core.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for adjusting the saving goal.
 */
public record AdjustSavingGoalCommand(long id, BigDecimal goal, LocalDate targetDate)
        implements ApplicationEvent {
}
