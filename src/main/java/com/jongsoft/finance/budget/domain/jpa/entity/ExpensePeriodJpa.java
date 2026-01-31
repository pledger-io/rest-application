package com.jongsoft.finance.budget.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Introspected
@Table(name = "budget_period")
public class ExpensePeriodJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "bp_lower_bound")
    private BigDecimal lowerBound;

    @Column(name = "bp_upper_bound")
    private BigDecimal upperBound;

    @ManyToOne
    @JoinColumn
    private ExpenseJpa expense;

    @ManyToOne
    @JoinColumn
    private BudgetJpa budget;

    public ExpensePeriodJpa(
            BigDecimal lowerBound, BigDecimal upperBound, ExpenseJpa expense, BudgetJpa budget) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.expense = expense;
        this.budget = budget;
    }

    public ExpensePeriodJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public ExpenseJpa getExpense() {
        return expense;
    }

    public BudgetJpa getBudget() {
        return budget;
    }

    public void setLowerBound(BigDecimal lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(BigDecimal upperBound) {
        this.upperBound = upperBound;
    }
}
