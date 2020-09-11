package com.jongsoft.finance.jpa.user.entity;

import java.time.LocalDate;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "budget")
public class BudgetJpa extends EntityJpa {

    private double expectedIncome;

    @Column(name = "b_from")
    private LocalDate from;

    @Column(name = "b_until")
    private LocalDate until;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @OneToMany(mappedBy = "budget")
    private Set<ExpensePeriodJpa> expenses;

    @Builder
    private BudgetJpa(
            double expectedIncome,
            LocalDate from,
            LocalDate until,
            UserAccountJpa user,
            Set<ExpensePeriodJpa> expenses) {
        this.expectedIncome = expectedIncome;
        this.from = from;
        this.until = until;
        this.user = user;
        this.expenses = expenses;
    }

    public BudgetJpa() {
    }
}
