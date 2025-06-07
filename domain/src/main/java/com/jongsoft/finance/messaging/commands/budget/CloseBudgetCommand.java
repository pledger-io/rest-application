package com.jongsoft.finance.messaging.commands.budget;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.LocalDate;

public record CloseBudgetCommand(long id, LocalDate end) implements ApplicationEvent {

  public static void budgetClosed(long id, LocalDate end) {
    new CloseBudgetCommand(id, end).publish();
  }
}
