package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "budget_expense")
public class ExpenseJpa extends EntityJpa {

  private String name;
  private boolean archived;

  @ManyToOne @JoinColumn private UserAccountJpa user;

  @Builder
  private ExpenseJpa(String name, boolean archived, UserAccountJpa user) {
    this.name = name;
    this.archived = archived;
    this.user = user;
  }

  public ExpenseJpa() {}
}
