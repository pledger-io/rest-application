package com.jongsoft.finance.project.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.domain.commands.CreateTimeEntryCommand;
import com.jongsoft.finance.project.domain.commands.DeleteTimeEntryCommand;
import com.jongsoft.finance.project.domain.commands.UpdateTimeEntryCommand;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Introspected
public class TimeEntry implements Serializable {

    private Long id;
    private Project project;
    private LocalDate date;
    private BigDecimal hours;
    private String description;
    private boolean invoiced;

    // Used by the Mapper strategy
    TimeEntry(
            Long id,
            Project project,
            LocalDate date,
            BigDecimal hours,
            String description,
            boolean invoiced) {
        this.id = id;
        this.project = project;
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.invoiced = invoiced;
    }

    private TimeEntry(Project project, LocalDate date, BigDecimal hours, String description) {
        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero.");
        }

        this.project = project;
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.invoiced = false;
        CreateTimeEntryCommand.timeEntryCreated(project.getId(), date, hours, description);
    }

    public void update(LocalDate date, BigDecimal hours, String description) {
        if (invoiced) {
            throw StatusException.badRequest(
                    "Cannot update invoiced time entry.", "time.entry.invoiced");
        }

        if (hours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero.");
        }

        this.date = date;
        this.hours = hours;
        this.description = description;
        UpdateTimeEntryCommand.timeEntryUpdated(id, date, hours, description);
    }

    public void delete() {
        if (invoiced) {
            throw StatusException.badRequest(
                    "Cannot delete invoiced time entry.", "time.entry.invoiced");
        }
        DeleteTimeEntryCommand.timeEntryDeleted(id);
    }

    public void markInvoiced() {
        this.invoiced = true;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public static TimeEntry create(
            Project project, LocalDate date, BigDecimal hours, String description) {
        return new TimeEntry(project, date, hours, description);
    }
}
