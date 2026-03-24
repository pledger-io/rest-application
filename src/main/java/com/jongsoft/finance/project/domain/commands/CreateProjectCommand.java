package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record CreateProjectCommand(
        long clientId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean billable)
        implements ApplicationEvent {

    public static void projectCreated(
            long clientId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable) {
        new CreateProjectCommand(clientId, name, description, startDate, endDate, billable)
                .publish();
    }
}
