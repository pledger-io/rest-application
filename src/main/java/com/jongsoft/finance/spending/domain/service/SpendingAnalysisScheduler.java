package com.jongsoft.finance.spending.domain.service;

import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.spending.domain.model.AnalyzeJob;

import io.micronaut.scheduling.annotation.Scheduled;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;

@Singleton
@SpendingAnalyticsEnabled
class SpendingAnalysisScheduler {

    private final Logger log = LoggerFactory.getLogger(SpendingAnalysisScheduler.class);
    private final UserProvider userProvider;

    SpendingAnalysisScheduler(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Scheduled(cron = "${application.schedules.spending.planner.cron}")
    void analyzeMonthlySpendingPatterns() {
        log.info("Scheduling monthly spending analysis, for month {}.", YearMonth.now());
        for (var user : userProvider.lookup()) {
            InternalAuthenticationEvent.authenticate(user.getUsername().email());
            AnalyzeJob.create(user.getUsername(), YearMonth.now());
        }
    }
}
