package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;

public record ComputeHealth(YearMonth yearMonth) implements ApplicationEvent {

    public static void computeHealth(YearMonth yearMonth) {
        new ComputeHealth(yearMonth).publish();
    }
}
