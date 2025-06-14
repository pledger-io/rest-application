package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.messaging.ApplicationEvent;

public record CreateSpendingInsight(SpendingInsight spendingInsight) implements ApplicationEvent {

  public static void createSpendingInsight(SpendingInsight spendingInsight) {
    new CreateSpendingInsight(spendingInsight).publish();
  }
}
