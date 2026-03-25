package com.jongsoft.finance.project.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.project.domain.commands.ArchiveProjectCommand;
import com.jongsoft.finance.project.domain.commands.CreateProjectCommand;
import com.jongsoft.finance.project.domain.commands.UpdateProjectCommand;

import io.micronaut.core.annotation.Introspected;

import java.io.Serializable;
import java.time.LocalDate;

@Introspected
public class Project implements Serializable {

    private Long id;
    private String name;
    private String description;
    private ClientIdentifier client;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean billable;
    private boolean archived;

    // Used by the Mapper strategy
    Project(
            Long id,
            String name,
            String description,
            ClientIdentifier client,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable,
            boolean archived) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.client = client;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billable = billable;
        this.archived = archived;
    }

    private Project(
            String name,
            String description,
            ClientIdentifier client,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        this.name = name;
        this.description = description;
        this.client = client;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billable = billable;
        this.archived = false;
        CreateProjectCommand.projectCreated(
                client.id(), name, description, startDate, endDate, billable);
    }

    public void update(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billable = billable;
        UpdateProjectCommand.projectUpdated(id, name, description, startDate, endDate, billable);
    }

    public void archive() {
        if (archived) {
            throw StatusException.badRequest(
                    "Project is already archived.", "project.already.archived");
        }
        this.archived = true;
        ArchiveProjectCommand.projectArchived(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ClientIdentifier getClient() {
        return client;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isBillable() {
        return billable;
    }

    public boolean isArchived() {
        return archived;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static Project create(
            String name,
            String description,
            ClientIdentifier client,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable) {
        return new Project(name, description, client, startDate, endDate, billable);
    }
}
