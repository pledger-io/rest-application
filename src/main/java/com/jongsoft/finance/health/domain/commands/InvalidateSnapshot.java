package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;

public record InvalidateSnapshot(YearMonth yearMonth) implements ApplicationEvent {

    public static void invalidate(YearMonth yearMonth) {
        new InvalidateSnapshot(yearMonth).publish();
    }
}
