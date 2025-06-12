package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateSpendingPattern(SpendingPattern spendingPattern) implements ApplicationEvent {

  public static void createSpendingPattern(SpendingPattern spendingPattern) {
    new CreateSpendingPattern(spendingPattern)
        .publish();
  }
}
