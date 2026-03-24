package com.jongsoft.finance.project.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

public record UpdateProjectCommand(
        long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean billable)
        implements ApplicationEvent {

    public static void projectUpdated(
            long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable) {
        new UpdateProjectCommand(id, name, description, startDate, endDate, billable).publish();
    }
}
