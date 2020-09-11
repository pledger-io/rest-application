package com.jongsoft.finance.jpa.user.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "budget_expense")
public class ExpenseJpa extends EntityJpa {

    private String name;
    private boolean archived;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @Builder
    private ExpenseJpa(String name, boolean archived, UserAccountJpa user) {
        this.name = name;
        this.archived = archived;
        this.user = user;
    }

    public ExpenseJpa() {
    }
}
