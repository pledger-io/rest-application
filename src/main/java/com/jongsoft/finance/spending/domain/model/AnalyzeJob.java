package com.jongsoft.finance.spending.domain.model;

import com.jongsoft.finance.StatusException;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.finance.spending.domain.commands.CompleteAnalyzeJob;
import com.jongsoft.finance.spending.domain.commands.CreateAnalyzeJob;
import com.jongsoft.finance.spending.domain.commands.FailAnalyzeJob;

import io.micronaut.core.annotation.Introspected;

import java.time.YearMonth;
import java.util.UUID;

@Introspected
public class AnalyzeJob {

    private final String jobId;
    private final YearMonth month;
    private final UserIdentifier user;
    private boolean completed;

    private AnalyzeJob(UserIdentifier user, YearMonth month) {
        this.jobId = UUID.randomUUID().toString();
        this.month = month;
        this.user = user;

        CreateAnalyzeJob.createAnalyzeJob(user, month);
    }

    AnalyzeJob(String jobId, YearMonth month, UserIdentifier user, boolean completed) {
        this.jobId = jobId;
        this.month = month;
        this.user = user;
        this.completed = completed;
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

    public String getJobId() {
        return jobId;
    }

    public YearMonth getMonth() {
        return month;
    }

    public UserIdentifier getUser() {
        return user;
    }

    public boolean isCompleted() {
        return completed;
    }

    public static AnalyzeJob create(UserIdentifier user, YearMonth month) {
        return new AnalyzeJob(user, month);
    }
}
