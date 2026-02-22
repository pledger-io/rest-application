package com.jongsoft.finance.contract.domain.jpa.entity;

import com.jongsoft.finance.banking.domain.jpa.entity.AccountJpa;
import com.jongsoft.finance.banking.domain.jpa.entity.TransactionScheduleJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Introspected
@Table(name = "contract")
public class ContractJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String fileToken;

    @ManyToOne
    private AccountJpa company;

    @ManyToOne
    private UserAccountJpa user;

    @OneToOne
    private TransactionScheduleJpa schedule;

    private boolean warningActive;
    private boolean archived;
    private boolean notificationSend;

    public ContractJpa() {}

    private ContractJpa(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            AccountJpa company,
            UserAccountJpa user) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.company = company;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getFileToken() {
        return fileToken;
    }

    public AccountJpa getCompany() {
        return company;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public boolean isWarningActive() {
        return warningActive;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isNotificationSend() {
        return notificationSend;
    }

    public TransactionScheduleJpa getSchedule() {
        return schedule;
    }

    public static ContractJpa create(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            AccountJpa company,
            UserAccountJpa user) {
        return new ContractJpa(name, description, startDate, endDate, company, user);
    }
}
