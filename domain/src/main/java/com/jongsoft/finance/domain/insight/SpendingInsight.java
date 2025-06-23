package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.messaging.commands.insight.CreateSpendingInsight;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SpendingInsight implements Insight {

  private final InsightType type;
  private final String category;
  private final Severity severity;
  private final double score;
  private final LocalDate detectedDate;
  private final String message;
  private final Long transactionId;
  private final Map<String, Object> metadata;

  @Builder
  private SpendingInsight(
      InsightType type,
      String category,
      Severity severity,
      double score,
      Long transactionId,
      LocalDate detectedDate,
      String message,
      Map<String, Object> metadata) {
    this.type = type;
    this.category = category;
    this.severity = severity;
    this.score = score;
    this.detectedDate = detectedDate;
    this.message = message;
    this.transactionId = transactionId;
    this.metadata = metadata;
  }

  @Override
  public void signal() {
    CreateSpendingInsight.createSpendingInsight(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SpendingInsight other) {
      return this.type == other.type
          && Objects.equals(this.detectedDate, other.detectedDate)
          && this.category.equalsIgnoreCase(other.category)
          && Objects.equals(this.transactionId, other.transactionId);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, detectedDate, category, transactionId);
  }

  @Override
  public String toString() {
    return "[%s] %s (severity:%s, detectedDate:%s): %s"
        .formatted(type, category, severity, detectedDate, transactionId);
  }
}
