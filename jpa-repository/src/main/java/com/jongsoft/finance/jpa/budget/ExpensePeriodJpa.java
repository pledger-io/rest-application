package com.jongsoft.finance.jpa.budget;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "budget_period")
public class ExpensePeriodJpa extends EntityJpa {

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

    @Builder
    private ExpensePeriodJpa(BigDecimal lowerBound, BigDecimal upperBound, ExpenseJpa expense, BudgetJpa budget) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.expense = expense;
        this.budget = budget;
    }

    public ExpensePeriodJpa() {
    }
}
