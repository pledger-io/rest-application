package com.jongsoft.finance.jpa.rule;

import java.util.List;

import javax.persistence.*;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule")
public class RuleJpa extends EntityJpa {

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

    @OneToMany(mappedBy = "rule", orphanRemoval = true, cascade = {CascadeType.ALL})
    private List<RuleConditionJpa> conditions;

    @OneToMany(mappedBy = "rule", orphanRemoval = true, cascade = {CascadeType.ALL})
    private List<RuleChangeJpa> changes;

    @Builder
    private RuleJpa(
            Long id,
            String name,
            String description,
            boolean restrictive,
            boolean active,
            boolean archived,
            int sort,
            UserAccountJpa user,
            RuleGroupJpa group,
            List<RuleConditionJpa> conditions,
            List<RuleChangeJpa> changes) {
        super(id);
        this.name = name;
        this.description = description;
        this.restrictive = restrictive;
        this.active = active;
        this.archived = archived;
        this.sort = sort;
        this.user = user;
        this.group = group;
        this.conditions = conditions;
        this.changes = changes;
    }

    public RuleJpa() {
    }

    public void setConditions(final List<RuleConditionJpa> conditions) {
        this.conditions = conditions;
    }

    public void setChanges(final List<RuleChangeJpa> changes) {
        this.changes = changes;
    }
}
