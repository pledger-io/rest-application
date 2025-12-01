package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.messaging.ApplicationEvent;

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

    public static void createSpendingInsight(SpendingInsight spendingInsight) {
        new CreateSpendingInsight(
                        spendingInsight.getType(),
                        spendingInsight.getCategory(),
                        spendingInsight.getSeverity(),
                        spendingInsight.getScore(),
                        spendingInsight.getDetectedDate(),
                        spendingInsight.getMessage(),
                        spendingInsight.getTransactionId(),
                        spendingInsight.getMetadata())
                .publish();
    }
}
