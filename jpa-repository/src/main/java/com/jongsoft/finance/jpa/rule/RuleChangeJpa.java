package com.jongsoft.finance.jpa.rule;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule_change")
public class RuleChangeJpa extends EntityJpa {

    @Enumerated(value = EnumType.STRING)
    private RuleColumn field;

    private String value;

    @ManyToOne
    @JoinColumn
    private RuleJpa rule;

    public RuleChangeJpa() {
        super();
    }

    @Builder
    private RuleChangeJpa(Long id, RuleColumn field, String value, RuleJpa rule) {
        super(id);
        this.field = field;
        this.value = value;
        this.rule = rule;
    }

}
