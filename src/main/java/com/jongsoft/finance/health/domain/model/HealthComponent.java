package com.jongsoft.finance.health.domain.model;

import com.jongsoft.finance.health.types.UnavailableReason;

import java.util.Map;

public record HealthComponent(
        boolean available,
        UnavailableReason unavailableReason,
        double value,
        int score,
        double weight,
        Map<String, Number> details) {}
