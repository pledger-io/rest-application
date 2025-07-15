package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.YearMonth;

public record CompleteAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

  public static void completeAnalyzeJob(UserIdentifier user, YearMonth month) {
    new CompleteAnalyzeJob(user, month).publish();
  }
}
