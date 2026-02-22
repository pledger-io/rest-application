package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Introspected
@DynamicInsert
@Table(name = "saving_goal")
public class SavingGoalJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "target_date", columnDefinition = "DATE", nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private BigDecimal goal;

    private BigDecimal allocated;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(value = EnumType.STRING)
    private Periodicity periodicity;

    @Column(name = "reoccurrence")
    private int interval;

    @ManyToOne
    private AccountJpa account;

    private boolean archived;

    private SavingGoalJpa(BigDecimal goal, LocalDate targetDate, String name, AccountJpa account) {
        this.targetDate = targetDate;
        this.goal = goal;
        this.name = name;
        this.account = account;
    }

    public SavingGoalJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public BigDecimal getGoal() {
        return goal;
    }

    public BigDecimal getAllocated() {
        return allocated;
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

    public AccountJpa getAccount() {
        return account;
    }

    public boolean isArchived() {
        return archived;
    }

    public static SavingGoalJpa of(
            BigDecimal goal, LocalDate targetDate, String name, AccountJpa account) {
        return new SavingGoalJpa(goal, targetDate, name, account);
    }
}
