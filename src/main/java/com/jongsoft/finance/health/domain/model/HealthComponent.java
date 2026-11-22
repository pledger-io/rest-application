package com.jongsoft.finance.health.domain.model;

import com.jongsoft.finance.health.types.UnavailableReason;

import java.util.Map;

public record HealthComponent(
        boolean available,
        UnavailableReason unavailableReason,
        double value,
        int score,
        double weight,
        Map<String, Double> details) {

    static HealthComponent notComputed() {
        return new HealthComponent(false, null, 0D, 0, 0D, Map.of());
    }
}
