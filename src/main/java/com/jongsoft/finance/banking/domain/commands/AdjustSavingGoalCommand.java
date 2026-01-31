package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Command for adjusting the saving goal. */
public record AdjustSavingGoalCommand(long id, BigDecimal goal, LocalDate targetDate)
        implements ApplicationEvent {

    public static void savingGoalAdjusted(long id, BigDecimal goal, LocalDate targetDate) {
        new AdjustSavingGoalCommand(id, goal, targetDate).publish();
    }
}
