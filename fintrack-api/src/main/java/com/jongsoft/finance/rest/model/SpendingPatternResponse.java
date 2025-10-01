package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

@Serdeable.Serializable
public class SpendingPatternResponse {
    private final SpendingPattern pattern;

    public SpendingPatternResponse(SpendingPattern pattern) {
        this.pattern = pattern;
    }

    @Schema(description = "The type of pattern")
    public PatternType getType() {
        return pattern.getType();
    }

    @Schema(description = "The category of the pattern")
    public String getCategory() {
        return pattern.getCategory();
    }

    @Schema(description = "The confidence score of the pattern (0.0 to 1.0)")
    public double getConfidence() {
        return pattern.getConfidence();
    }

    @Schema(
            description = "The date when the pattern was detected",
            implementation = String.class,
            format = "yyyy-mm-dd")
    public LocalDate getDetectedDate() {
        return pattern.getDetectedDate();
    }

    @Schema(description = "Additional metadata for the pattern")
    public Map<String, ?> getMetadata() {
        return pattern.getMetadata();
    }
}
