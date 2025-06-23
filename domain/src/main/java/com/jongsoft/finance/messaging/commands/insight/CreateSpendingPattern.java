package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.LocalDate;
import java.util.Map;

public record CreateSpendingPattern(
    PatternType type,
    String category,
    double confidence,
    LocalDate detectedDate,
    Map<String, ?> metadata)
    implements ApplicationEvent {

  public static void createSpendingPattern(SpendingPattern spendingPattern) {
    new CreateSpendingPattern(
            spendingPattern.getType(),
            spendingPattern.getCategory(),
            spendingPattern.getConfidence(),
            spendingPattern.getDetectedDate(),
            spendingPattern.getMetadata())
        .publish();
  }
}
