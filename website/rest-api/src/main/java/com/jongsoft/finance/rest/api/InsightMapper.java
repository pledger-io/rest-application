package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.rest.model.InsightResponse;
import com.jongsoft.finance.rest.model.InsightResponseSeverity;
import com.jongsoft.finance.rest.model.InsightResponseType;
import com.jongsoft.finance.rest.model.PatternResponse;

import java.util.Map;

public interface InsightMapper {

    static InsightResponse toInsightResponse(SpendingInsight insight) {
        return new InsightResponse(
                InsightResponseType.valueOf(insight.getType().name()),
                insight.getCategory(),
                InsightResponseSeverity.valueOf(insight.getSeverity().name()),
                insight.getScore(),
                insight.getDetectedDate(),
                insight.getMessage(),
                Map.copyOf(insight.getMetadata()));
    }

    static PatternResponse toPatternResponse(SpendingPattern pattern) {
        var response = new PatternResponse();
        response.category(pattern.getCategory());
        response.confidence(pattern.getConfidence());
        response.detectedDate(pattern.getDetectedDate());
        response.metadata(Map.copyOf(pattern.getMetadata()));
        return response;
    }
}
