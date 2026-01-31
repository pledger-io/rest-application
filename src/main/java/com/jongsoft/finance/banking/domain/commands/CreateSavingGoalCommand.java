package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A command to create a new saving goal in the system for the authenticated user. */
public record CreateSavingGoalCommand(
        long accountId, String name, BigDecimal goal, LocalDate targetDate)
        implements ApplicationEvent {

    public static void savingGoalCreated(
            long accountId, String name, BigDecimal goal, LocalDate targetDate) {
        new CreateSavingGoalCommand(accountId, name, goal, targetDate).publish();
    }
}
