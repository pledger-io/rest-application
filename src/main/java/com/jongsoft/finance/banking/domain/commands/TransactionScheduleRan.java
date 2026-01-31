package com.jongsoft.finance.banking.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record TransactionScheduleRan(long id, LocalDate runDate, LocalDate nextRunDate)
        implements ApplicationEvent {

    public static void scheduleRan(long id, LocalDate runDate, LocalDate nextRunDate) {
        new TransactionScheduleRan(id, runDate, nextRunDate).publish();
    }
}
