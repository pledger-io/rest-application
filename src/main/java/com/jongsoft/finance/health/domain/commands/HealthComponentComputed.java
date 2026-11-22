package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;
import java.util.Map;

public record HealthComponentComputed(
        YearMonth yearMonth,
        ComponentType type,
        int score,
        double value,
        double weight,
        Map<String, Double> details)
        implements ApplicationEvent {
    public enum ComponentType {
        DEBT,
        SAVINGS,
        BUDGET
    }

    public static void componentComputed(
            YearMonth yearMonth,
            ComponentType type,
            int score,
            double value,
            double weight,
            Map<String, Double> details) {
        new HealthComponentComputed(yearMonth, type, score, value, weight, details).publish();
    }
}
