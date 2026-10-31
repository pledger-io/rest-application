package com.jongsoft.finance.health.domain.model;

import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

public class HealthSnapshot {

    public record Configuration(int windowMonths, int algorithmVersion) {}

    public record HealthScore(int score, HealthRating rating) {}

    private YearMonth period;
    private HealthSnapshotStatus status;
    private Configuration configuration;
    private LocalDateTime computedAt;
    private Currency currency;

    private HealthScore healthScore;

    private HealthComponent savingsRate;
    private HealthComponent budgetAdherence;
    private HealthComponent debtToIncome;
}
