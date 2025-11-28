package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule_condition")
public class RuleConditionJpa extends EntityJpa {

    @Enumerated(value = EnumType.STRING)
    private RuleColumn field;

    @Enumerated(value = EnumType.STRING)
    private RuleOperation operation;

    @Column(name = "cond_value")
    private String condition;

    @ManyToOne
    @JoinColumn
    private RuleJpa rule;

    @Builder
    private RuleConditionJpa(
            Long id, RuleColumn field, RuleOperation operation, String condition, RuleJpa rule) {
        super(id);
        this.field = field;
        this.operation = operation;
        this.condition = condition;
        this.rule = rule;
    }

    public RuleConditionJpa() {}
}
