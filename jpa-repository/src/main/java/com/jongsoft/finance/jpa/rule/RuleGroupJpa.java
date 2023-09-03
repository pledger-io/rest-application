package com.jongsoft.finance.jpa.rule;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule_group")
public class RuleGroupJpa extends EntityJpa {

    private String name;
    private int sort;
    private boolean archived;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @Builder
    private RuleGroupJpa(String name, int sort, boolean archived, UserAccountJpa user) {
        this.name = name;
        this.sort = sort;
        this.archived = archived;
        this.user = user;
    }

    public RuleGroupJpa() {
    }

}
