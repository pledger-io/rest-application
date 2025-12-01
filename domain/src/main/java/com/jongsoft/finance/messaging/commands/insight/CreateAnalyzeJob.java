package com.jongsoft.finance.messaging.commands.insight;

import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.ApplicationEvent;

import java.time.YearMonth;

public record CreateAnalyzeJob(UserIdentifier user, YearMonth month) implements ApplicationEvent {

    public static void createAnalyzeJob(UserIdentifier user, YearMonth month) {
        new CreateAnalyzeJob(user, month).publish();
    }
}
