package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.YearMonth;

public record CreateAnalyzeJob(YearMonth month) implements ApplicationEvent {

  public static void createAnalyzeJob(YearMonth month) {
    new CreateAnalyzeJob(month).publish();
  }
}
