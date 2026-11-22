package com.jongsoft.finance.health.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;

/**
 * Instruction to start the calculation of health snapshot for a given year month.
 * @param yearMonth the year month for which the health snapshot should be computed
 */
public record ComputeHealth(YearMonth yearMonth) implements ApplicationEvent {

    public static void computeHealth(YearMonth yearMonth) {
        new ComputeHealth(yearMonth).publish();
    }
}
