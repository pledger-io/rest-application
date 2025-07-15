package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.YearMonth;

public record FailAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

  public static void failAnalyzeJob(UserIdentifier user, YearMonth month) {
    new FailAnalyzeJob(user, month).publish();
  }
}
