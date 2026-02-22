package com.jongsoft.finance.budget.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Introspected
@Table(name = "budget")
public class BudgetJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private double expectedIncome;

    @Column(name = "b_from")
    private LocalDate from;

    @Column(name = "b_until")
    private LocalDate until;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @OneToMany(mappedBy = "budget", fetch = FetchType.EAGER)
    private Set<ExpensePeriodJpa> expenses;

    public BudgetJpa(double expectedIncome, LocalDate from, UserAccountJpa user) {
        this.expectedIncome = expectedIncome;
        this.from = from;
        this.user = user;
        this.expenses = new HashSet<>();
    }

    public BudgetJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public double getExpectedIncome() {
        return expectedIncome;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getUntil() {
        return until;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public Set<ExpensePeriodJpa> getExpenses() {
        return expenses;
    }
}
