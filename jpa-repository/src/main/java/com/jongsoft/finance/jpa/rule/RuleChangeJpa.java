package com.jongsoft.finance.jpa.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule_change")
public class RuleChangeJpa extends EntityJpa {

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

  @Builder
  private RuleChangeJpa(Long id, RuleColumn field, String value, RuleJpa rule) {
    super(id);
    this.field = field;
    this.value = value;
    this.rule = rule;
  }
}
