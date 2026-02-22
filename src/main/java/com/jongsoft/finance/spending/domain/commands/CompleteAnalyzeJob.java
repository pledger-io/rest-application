package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

import java.time.YearMonth;

public record CompleteAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

    public static void completeAnalyzeJob(UserIdentifier user, YearMonth month) {
        new CompleteAnalyzeJob(user, month).publish();
    }
}
