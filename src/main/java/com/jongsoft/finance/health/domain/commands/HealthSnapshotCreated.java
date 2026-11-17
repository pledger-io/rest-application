package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;
import java.util.Currency;

public record HealthSnapshotCreated(
        YearMonth yearMonth, int windowMonths, int algorithmVersion, Currency currency)
        implements ApplicationEvent {
    public static void healthSnapshotCreated(
            YearMonth yearMonth, int windowMonths, int algorithmVersion, Currency currency) {
        new HealthSnapshotCreated(yearMonth, windowMonths, algorithmVersion, currency).publish();
    }
}
