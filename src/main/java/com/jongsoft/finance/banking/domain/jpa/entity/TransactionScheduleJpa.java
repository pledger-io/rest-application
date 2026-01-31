package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Introspected
@Table(name = "transaction_schedule")
public class TransactionScheduleJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "start_date", columnDefinition = "DATE")
    private LocalDate start;

    @Column(name = "end_date", columnDefinition = "DATE")
    private LocalDate end;

    @Column(name = "last_run", columnDefinition = "DATE")
    private LocalDate lastRun;

    @Column(name = "next_run", columnDefinition = "DATE")
    private LocalDate nextRun;

    private double amount;
    private String name;
    private String description;

    @Enumerated(value = EnumType.STRING)
    private Periodicity periodicity;

    @Column(name = "reoccur")
    private int interval;

    @ManyToOne
    private UserAccountJpa user;

    @ManyToOne
    private AccountJpa source;

    @ManyToOne
    private AccountJpa destination;

    //    @ManyToOne
    //    private ContractJpa contract;

    public TransactionScheduleJpa(
            double amount,
            String name,
            Periodicity periodicity,
            int interval,
            UserAccountJpa user,
            AccountJpa source,
            AccountJpa destination) {
        this.amount = amount;
        this.name = name;
        this.periodicity = periodicity;
        this.interval = interval;
        this.user = user;
        this.source = source;
        this.destination = destination;
    }

    public TransactionScheduleJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public LocalDate getLastRun() {
        return lastRun;
    }

    public LocalDate getNextRun() {
        return nextRun;
    }

    public double getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public int getInterval() {
        return interval;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public AccountJpa getSource() {
        return source;
    }

    public AccountJpa getDestination() {
        return destination;
    }
}
