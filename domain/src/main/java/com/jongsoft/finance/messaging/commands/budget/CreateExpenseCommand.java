package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseCommand(String name, LocalDate start, BigDecimal budget)
    implements ApplicationEvent {

  public static void expenseCreated(String name, LocalDate start, BigDecimal budget) {
    new CreateExpenseCommand(name, start, budget).publish();
  }
}
