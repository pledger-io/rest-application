package com.jongsoft.finance.spending.adapter.rest;

import com.jongsoft.finance.rest.model.DetectedInsightResponse;
import com.jongsoft.finance.rest.model.DetectedInsightResponseSeverity;
import com.jongsoft.finance.rest.model.DetectedInsightResponseType;
import com.jongsoft.finance.rest.model.DetectedPatternResponse;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;

import java.util.Map;

public interface InsightMapper {

    static DetectedInsightResponse toInsightResponse(SpendingInsight insight) {
        return new DetectedInsightResponse(
                DetectedInsightResponseType.valueOf(insight.getType().name()),
                insight.getCategory(),
                DetectedInsightResponseSeverity.valueOf(insight.getSeverity().name()),
                insight.getScore(),
                insight.getDetectedDate(),
                insight.getMessage(),
                Map.copyOf(insight.getMetadata()));
    }

    static DetectedPatternResponse toPatternResponse(SpendingPattern pattern) {
        var response = new DetectedPatternResponse();
        response.category(pattern.getCategory());
        response.confidence(pattern.getConfidence());
        response.detectedDate(pattern.getDetectedDate());
        response.metadata(Map.copyOf(pattern.getMetadata()));
        return response;
    }
}
