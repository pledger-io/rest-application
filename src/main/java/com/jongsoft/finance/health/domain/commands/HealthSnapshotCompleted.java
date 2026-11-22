package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;

import java.time.YearMonth;

public record HealthSnapshotCompleted(
        YearMonth yearMonth, HealthRating healthRating, int score, HealthSnapshotStatus status)
        implements ApplicationEvent {

    public static void completedComputation(YearMonth yearMonth, HealthRating rating, int score) {
        new HealthSnapshotCompleted(yearMonth, rating, score, HealthSnapshotStatus.COMPLETE)
                .publish();
    }

    public static void insufficientTransactions(YearMonth yearMonth) {
        new HealthSnapshotCompleted(
                        yearMonth, HealthRating.AT_RISK, 0, HealthSnapshotStatus.INSUFFICIENT_DATA)
                .publish();
    }
}
