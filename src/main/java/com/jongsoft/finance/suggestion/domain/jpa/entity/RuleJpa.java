package com.jongsoft.finance.suggestion.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "rule")
public class RuleJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private String description;
    private boolean restrictive;
    private boolean active;
    private boolean archived;
    private int sort;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @ManyToOne
    @JoinColumn
    private RuleGroupJpa group;

    @OneToMany(
            mappedBy = "rule",
            orphanRemoval = true,
            cascade = {CascadeType.ALL})
    private List<RuleConditionJpa> conditions;

    @OneToMany(
            mappedBy = "rule",
            orphanRemoval = true,
            cascade = {CascadeType.ALL})
    private List<RuleChangeJpa> changes;

    public RuleJpa(
            String name,
            String description,
            boolean restrictive,
            boolean active,
            int sort,
            UserAccountJpa user,
            RuleGroupJpa group) {
        this.name = name;
        this.description = description;
        this.restrictive = restrictive;
        this.active = active;
        this.archived = false;
        this.sort = sort;
        this.user = user;
        this.group = group;
    }

    public RuleJpa() {}

    public void setConditions(final List<RuleConditionJpa> conditions) {
        this.conditions = conditions;
    }

    public void setChanges(final List<RuleChangeJpa> changes) {
        this.changes = changes;
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

    public boolean isRestrictive() {
        return restrictive;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isArchived() {
        return archived;
    }

    public int getSort() {
        return sort;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public RuleGroupJpa getGroup() {
        return group;
    }

    public List<RuleConditionJpa> getConditions() {
        return conditions;
    }

    public List<RuleChangeJpa> getChanges() {
        return changes;
    }
}
