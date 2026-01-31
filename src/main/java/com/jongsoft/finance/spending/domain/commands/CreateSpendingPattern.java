package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.spending.types.PatternType;

import java.time.LocalDate;
import java.util.Map;

public record CreateSpendingPattern(
        PatternType type,
        String category,
        double confidence,
        LocalDate detectedDate,
        Map<String, ?> metadata)
        implements ApplicationEvent {

    public static void createSpendingPattern(
            PatternType type,
            String category,
            double confidence,
            LocalDate detectedDate,
            Map<String, ?> metadata) {
        new CreateSpendingPattern(type, category, confidence, detectedDate, metadata).publish();
    }
}
