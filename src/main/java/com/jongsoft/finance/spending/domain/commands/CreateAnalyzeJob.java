package com.jongsoft.finance.spending.domain.commands;

import com.jongsoft.finance.ApplicationEvent;
import com.jongsoft.finance.core.value.UserIdentifier;

import java.time.YearMonth;

public record CreateAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

    public static void createAnalyzeJob(UserIdentifier user, YearMonth month) {
        new CreateAnalyzeJob(user, month).publish();
    }
}
