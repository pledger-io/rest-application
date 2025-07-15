package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.YearMonth;

public record CleanInsightsForMonth(YearMonth month) implements ApplicationEvent {

  public static void cleanInsightsForMonth(YearMonth month) {
    new CleanInsightsForMonth(month).publish();
  }
}
