package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

@Serdeable.Serializable
public class SpendingInsightResponse {
    private final SpendingInsight insight;

    public SpendingInsightResponse(SpendingInsight insight) {
        this.insight = insight;
    }

    @Schema(description = "The type of insight")
    public InsightType getType() {
        return insight.getType();
    }

    @Schema(description = "The category of the insight")
    public String getCategory() {
        return insight.getCategory();
    }

    @Schema(description = "The severity of the insight")
    public Severity getSeverity() {
        return insight.getSeverity();
    }

    @Schema(description = "The confidence score of the insight (0.0 to 1.0)")
    public double getScore() {
        return insight.getScore();
    }

    @Schema(
            description = "The date when the insight was detected",
            implementation = String.class,
            format = "yyyy-mm-dd")
    public LocalDate getDetectedDate() {
        return insight.getDetectedDate();
    }

    @Schema(description = "The message describing the insight")
    public String getMessage() {
        return insight.getMessage();
    }

    @Schema(description = "The ID of the transaction related to this insight, if any")
    public Long getTransactionId() {
        return insight.getTransactionId();
    }

    @Schema(description = "Additional metadata for the insight")
    public Map<String, Object> getMetadata() {
        return insight.getMetadata();
    }
}
