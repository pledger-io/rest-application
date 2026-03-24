package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTimeEntryCommand(
        long projectId, LocalDate date, BigDecimal hours, String description)
        implements ApplicationEvent {

    public static void timeEntryCreated(
            long projectId, LocalDate date, BigDecimal hours, String description) {
        new CreateTimeEntryCommand(projectId, date, hours, description).publish();
    }
}
