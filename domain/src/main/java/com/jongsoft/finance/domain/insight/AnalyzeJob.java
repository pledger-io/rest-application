package com.jongsoft.finance.domain.insight;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.commands.insight.CompleteAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.CreateAnalyzeJob;
import com.jongsoft.finance.messaging.commands.insight.FailAnalyzeJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AnalyzeJob {

    private final String jobId;
    private final YearMonth month;
    private final UserIdentifier user;
    private boolean completed;

    public AnalyzeJob(UserIdentifier user, YearMonth month) {
        this.jobId = UUID.randomUUID().toString();
        this.month = month;
        this.user = user;

        CreateAnalyzeJob.createAnalyzeJob(user, month);
    }

    public void complete() {
        if (completed) {
            throw StatusException.badRequest(
                    "Cannot complete an analyze job that has already completed.",
                    "AnalyzeJob.completed");
        }

        completed = true;
        CompleteAnalyzeJob.completeAnalyzeJob(user, month);
    }

    public void fail() {
        if (completed) {
            throw StatusException.badRequest(
                    "Cannot fail an analyze job that has already completed.",
                    "AnalyzeJob.completed");
        }

        FailAnalyzeJob.failAnalyzeJob(user, month);
    }
}
