package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.messaging.ApplicationEvent;

import java.time.YearMonth;

public record CompleteAnalyzeJob(YearMonth month) implements ApplicationEvent {

  public static void completeAnalyzeJob(YearMonth month) {
    new CompleteAnalyzeJob(month)
        .publish();
  }
}
