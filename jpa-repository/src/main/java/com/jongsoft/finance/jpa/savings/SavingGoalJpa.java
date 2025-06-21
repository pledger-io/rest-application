package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.schedule.Periodicity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;

@Getter
@Entity
@DynamicInsert
@Table(name = "saving_goal")
public class SavingGoalJpa extends EntityJpa {

  @Column(name = "target_date", columnDefinition = "DATE", nullable = false)
  private LocalDate targetDate;

  @Column(nullable = false)
  private BigDecimal goal;

  private BigDecimal allocated;

  @Column(nullable = false)
  private String name;

  private String description;

  @Enumerated(value = EnumType.STRING)
  private Periodicity periodicity;

  @Column(name = "reoccurrence")
  private int interval;

  @ManyToOne
  private AccountJpa account;

  private boolean archived;

  @Builder
  public SavingGoalJpa(
      Long id,
      LocalDate targetDate,
      BigDecimal goal,
      BigDecimal allocated,
      String name,
      String description,
      Periodicity periodicity,
      int interval,
      AccountJpa account) {
    super(id);
    this.targetDate = targetDate;
    this.goal = goal;
    this.allocated = allocated;
    this.name = name;
    this.description = description;
    this.periodicity = periodicity;
    this.interval = interval;
    this.account = account;
  }

  public SavingGoalJpa() {}
}
