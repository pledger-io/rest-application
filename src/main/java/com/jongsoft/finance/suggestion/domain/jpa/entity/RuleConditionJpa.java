package com.jongsoft.finance.suggestion.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.finance.suggestion.types.RuleOperation;

import jakarta.persistence.*;

@Entity
@Table(name = "rule_condition")
public class RuleConditionJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private RuleColumn field;

    @Enumerated(value = EnumType.STRING)
    private RuleOperation operation;

    @Column(name = "cond_value")
    private String condition;

    @ManyToOne
    @JoinColumn
    private RuleJpa rule;

    public RuleConditionJpa(
            RuleColumn field, RuleOperation operation, String condition, RuleJpa rule) {
        this.field = field;
        this.operation = operation;
        this.condition = condition;
        this.rule = rule;
    }

    public RuleConditionJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public RuleColumn getField() {
        return field;
    }

    public RuleOperation getOperation() {
        return operation;
    }

    public String getCondition() {
        return condition;
    }

    public RuleJpa getRule() {
        return rule;
    }
}
