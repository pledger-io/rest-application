package com.jongsoft.finance.health.domain.model;

import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

public class HealthSnapshot {

    public record Configuration(int windowMonths, int algorithmVersion) {}

    public record HealthScore(int score, HealthRating rating) {}

    private final YearMonth period;
    private final Configuration configuration;
    private final Currency currency;

    private HealthSnapshotStatus status;
    private LocalDateTime computedAt;
    private HealthScore healthScore;

    private HealthComponent savingsRate;
    private HealthComponent budgetAdherence;
    private HealthComponent debtToIncome;

    // Constructor for mappers
    HealthSnapshot(
        String period,
        int windowMonths,
        int algorithmVersion,
        Currency currency,
        HealthSnapshotStatus status,
        LocalDateTime computedAt,
        int score,
        HealthRating rating,
        HealthComponent savingRate,
        HealthComponent budgetAdherence,
        HealthComponent debtToIncome) {
        this.period = YearMonth.parse(period);
        this.configuration = new Configuration(windowMonths, algorithmVersion);
        this.currency = currency;
        this.status = status;
        this.computedAt = computedAt;
        this.healthScore = new HealthScore(score, rating);
        this.savingsRate = savingRate;
        this.budgetAdherence = budgetAdherence;
        this.debtToIncome = debtToIncome;
    }

    private HealthSnapshot(
            YearMonth yearMonth, int windowMonths, int algorithmVersion, Currency currency) {
        this.period = yearMonth;
        this.configuration = new Configuration(windowMonths, algorithmVersion);
        this.currency = currency;
        this.status = HealthSnapshotStatus.PROVISIONAL;

        budgetAdherence = HealthComponent.notComputed();
        savingsRate = HealthComponent.notComputed();
        debtToIncome = HealthComponent.notComputed();

        HealthSnapshotCreated.healthSnapshotCreated(
                period, windowMonths, algorithmVersion, currency);
    }

    // Creates and stores a new blank snapshot for the user
    public static HealthSnapshot of(
            YearMonth yearMonth, int windowMonths, int algorithmVersion, Currency currency) {
        return new HealthSnapshot(yearMonth, windowMonths, algorithmVersion, currency);
    }
}
