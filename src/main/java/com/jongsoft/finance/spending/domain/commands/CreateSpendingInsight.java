package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.finance.spending.types.Severity;

import java.time.LocalDate;
import java.util.Map;

public record CreateSpendingInsight(
        InsightType type,
        String category,
        Severity severity,
        double score,
        LocalDate detectedDate,
        String message,
        Long transactionId,
        Map<String, Object> metadata)
        implements ApplicationEvent {

    public static void createSpendingInsight(
            InsightType type,
            String category,
            Severity severity,
            double score,
            LocalDate detectedDate,
            String message,
            Long transactionId,
            Map<String, Object> metadata) {
        new CreateSpendingInsight(
                        type,
                        category,
                        severity,
                        score,
                        detectedDate,
                        message,
                        transactionId,
                        metadata)
                .publish();
    }
}
