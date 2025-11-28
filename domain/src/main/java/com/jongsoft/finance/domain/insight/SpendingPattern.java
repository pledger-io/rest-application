package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.messaging.commands.insight.CreateSpendingPattern;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Getter
public class SpendingPattern implements Insight {

    private final PatternType type;
    private final String category;
    private final double confidence;
    private final LocalDate detectedDate;
    private final Map<String, ?> metadata;

    @Builder
    private SpendingPattern(
            PatternType type,
            String category,
            double confidence,
            LocalDate detectedDate,
            Map<String, Object> metadata) {
        this.type = type;
        this.category = category;
        this.confidence = confidence;
        this.detectedDate = detectedDate;
        this.metadata = metadata;
    }

    @Override
    public void signal() {
        CreateSpendingPattern.createSpendingPattern(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpendingPattern other) {
            return this.type == other.type
                    && Objects.equals(this.detectedDate, other.detectedDate)
                    && this.category.equalsIgnoreCase(other.category);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, category, detectedDate);
    }

    @Override
    public String toString() {
        return "[%s] %s (confidence:%.2f%%, detectedDate:%s)"
                .formatted(type, category, confidence * 100, detectedDate);
    }
}
