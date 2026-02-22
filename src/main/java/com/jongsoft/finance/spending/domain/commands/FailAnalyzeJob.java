package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

import java.time.YearMonth;

public record FailAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

    public static void failAnalyzeJob(UserIdentifier user, YearMonth month) {
        new FailAnalyzeJob(user, month).publish();
    }
}
