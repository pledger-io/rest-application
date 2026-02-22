package com.jongsoft.finance.suggestion.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "rule_group")
public class RuleGroupJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private int sort;
    private boolean archived;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    public RuleGroupJpa(String name, int sort, UserAccountJpa user) {
        this.name = name;
        this.sort = sort;
        this.archived = false;
        this.user = user;
    }

    public RuleGroupJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public boolean isArchived() {
        return archived;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
