package com.jongsoft.finance.spending.domain.model;

import com.jongsoft.finance.spending.domain.commands.CreateSpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.finance.spending.types.Severity;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Introspected
public class SpendingInsight implements Insight {

    private final InsightType type;
    private final String category;
    private final Severity severity;
    private final double score;
    private final LocalDate detectedDate;
    private final String message;
    private final Long transactionId;
    private final Map<String, Object> metadata;

    public SpendingInsight(
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
        CreateSpendingInsight.createSpendingInsight(
                type, category, severity, score, detectedDate, message, transactionId, metadata);
    }

    public InsightType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public double getScore() {
        return score;
    }

    public LocalDate getDetectedDate() {
        return detectedDate;
    }

    public String getMessage() {
        return message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
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
