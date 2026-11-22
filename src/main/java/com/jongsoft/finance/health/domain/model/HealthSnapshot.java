package com.jongsoft.finance.health.domain.model;

import com.jongsoft.finance.health.domain.commands.HealthSnapshotCompleted;
import com.jongsoft.finance.health.domain.commands.HealthSnapshotCreated;
import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

@Introspected
public class HealthSnapshot {

    public record Configuration(int windowMonths, int algorithmVersion) {}

    public record HealthScore(Integer score, HealthRating rating) {}

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
    @Creator
    HealthSnapshot(
            String period,
            int windowMonths,
            int algorithmVersion,
            Currency currency,
            HealthSnapshotStatus status,
            LocalDateTime computedAt,
            Integer score,
            HealthRating rating,
            HealthComponent savingsRate,
            HealthComponent budgetAdherence,
            HealthComponent debtToIncome) {
        this.period = YearMonth.parse(period);
        this.configuration = new Configuration(windowMonths, algorithmVersion);
        this.currency = currency;
        this.status = status;
        this.computedAt = computedAt;
        this.healthScore = new HealthScore(score, rating);
        this.savingsRate = savingsRate;
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

    public void insufficientInformation() {
        status = HealthSnapshotStatus.INSUFFICIENT_DATA;
        computedAt = LocalDateTime.now();
        healthScore = new HealthScore(0, HealthRating.AT_RISK);
        HealthSnapshotCompleted.insufficientTransactions(period);
    }

    public void healthScoreComputed(int score, HealthRating rating) {
        status = HealthSnapshotStatus.COMPLETE;
        healthScore = new HealthScore(score, rating);
        computedAt = LocalDateTime.now();
        HealthSnapshotCompleted.completedComputation(period, rating, score);
    }

    // todo add the methods to register the computed health components

    public YearMonth getPeriod() {
        return period;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Currency getCurrency() {
        return currency;
    }

    public HealthSnapshotStatus getStatus() {
        return status;
    }

    public LocalDateTime getComputedAt() {
        return computedAt;
    }

    public HealthScore getHealthScore() {
        return healthScore;
    }

    public HealthComponent getSavingsRate() {
        return savingsRate;
    }

    public HealthComponent getBudgetAdherence() {
        return budgetAdherence;
    }

    public HealthComponent getDebtToIncome() {
        return debtToIncome;
    }
}
