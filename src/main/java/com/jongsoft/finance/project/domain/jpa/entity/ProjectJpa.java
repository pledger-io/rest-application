package com.jongsoft.finance.project.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Introspected
@Table(name = "project")
public class ProjectJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    private ClientJpa client;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean billable = true;
    private boolean archived;

    @ManyToOne
    private UserAccountJpa user;

    public ProjectJpa() {}

    private ProjectJpa(
            String name,
            String description,
            ClientJpa client,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable,
            UserAccountJpa user) {
        this.name = name;
        this.description = description;
        this.client = client;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billable = billable;
        this.archived = false;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ClientJpa getClient() {
        return client;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isBillable() {
        return billable;
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static ProjectJpa of(
            String name,
            String description,
            ClientJpa client,
            LocalDate startDate,
            LocalDate endDate,
            boolean billable,
            UserAccountJpa user) {
        return new ProjectJpa(name, description, client, startDate, endDate, billable, user);
    }
}
