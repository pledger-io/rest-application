package com.jongsoft.finance.suggestion.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.suggestion.types.RuleColumn;

import jakarta.persistence.*;

@Entity
@Table(name = "rule_change")
public class RuleChangeJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private RuleColumn field;

    @Column(name = "change_val")
    private String value;

    @ManyToOne
    @JoinColumn
    private RuleJpa rule;

    public RuleChangeJpa() {
        super();
    }

    public RuleChangeJpa(RuleColumn field, String value, RuleJpa rule) {
        this.field = field;
        this.value = value;
        this.rule = rule;
    }

    @Override
    public Long getId() {
        return id;
    }

    public RuleColumn getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public RuleJpa getRule() {
        return rule;
    }
}
