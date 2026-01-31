package com.jongsoft.finance.spending.domain.model;

import com.jongsoft.finance.spending.domain.commands.CreateSpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Introspected
public class SpendingPattern implements Insight {

    private final PatternType type;
    private final String category;
    private final double confidence;
    private final LocalDate detectedDate;
    private final Map<String, ?> metadata;

    public SpendingPattern(
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
        CreateSpendingPattern.createSpendingPattern(
                type, category, confidence, detectedDate, metadata);
    }

    public PatternType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public LocalDate getDetectedDate() {
        return detectedDate;
    }

    public Map<String, ?> getMetadata() {
        return metadata;
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
