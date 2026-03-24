package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTimeEntryCommand(long id, LocalDate date, BigDecimal hours, String description)
        implements ApplicationEvent {

    public static void timeEntryUpdated(
            long id, LocalDate date, BigDecimal hours, String description) {
        new UpdateTimeEntryCommand(id, date, hours, description).publish();
    }
}
