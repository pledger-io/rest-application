package com.jongsoft.finance.budget.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

@Entity
@Table(name = "budget_expense")
public class ExpenseJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private boolean archived;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    public ExpenseJpa(String name, UserAccountJpa user) {
        this.name = name;
        this.user = user;
    }

    public ExpenseJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isArchived() {
        return archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
