package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateBudgetCommand(Budget budget) implements ApplicationEvent {

  public static void budgetCreated(Budget budget) {
    new CreateBudgetCommand(budget).publish();
  }
}
