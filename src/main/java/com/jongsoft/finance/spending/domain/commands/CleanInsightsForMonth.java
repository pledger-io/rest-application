package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.YearMonth;

public record CleanInsightsForMonth(YearMonth month) implements ApplicationEvent {

    public static void cleanInsightsForMonth(YearMonth month) {
        new CleanInsightsForMonth(month).publish();
    }
}
