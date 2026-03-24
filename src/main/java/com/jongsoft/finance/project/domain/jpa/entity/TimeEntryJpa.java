package com.jongsoft.finance.project.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Introspected
@Table(name = "time_entry")
public class TimeEntryJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    private ProjectJpa project;

    private LocalDate date;
    private BigDecimal hours;
    private String description;
    private boolean invoiced;

    @ManyToOne
    private UserAccountJpa user;

    public TimeEntryJpa() {}

    private TimeEntryJpa(
            ProjectJpa project,
            LocalDate date,
            BigDecimal hours,
            String description,
            UserAccountJpa user) {
        this.project = project;
        this.date = date;
        this.hours = hours;
        this.description = description;
        this.invoiced = false;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public ProjectJpa getProject() {
        return project;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public void setInvoiced(boolean invoiced) {
        this.invoiced = invoiced;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static TimeEntryJpa of(
            ProjectJpa project,
            LocalDate date,
            BigDecimal hours,
            String description,
            UserAccountJpa user) {
        return new TimeEntryJpa(project, date, hours, description, user);
    }
}
