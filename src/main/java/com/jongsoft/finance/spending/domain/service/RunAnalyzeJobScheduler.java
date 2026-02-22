package com.jongsoft.finance.spending.domain.service;

import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.spending.adapter.api.AnalyzeJobProvider;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.transaction.annotation.Transactional;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@Singleton
@SpendingAnalyticsEnabled
class RunAnalyzeJobScheduler {

    private final Logger log = LoggerFactory.getLogger(RunAnalyzeJobScheduler.class);

    private final AnalyzeJobProvider analyzeJobProvider;
    private final AnalysisRunner analysisRunner;

    RunAnalyzeJobScheduler(AnalyzeJobProvider analyzeJobProvider, AnalysisRunner analysisRunner) {
        this.analyzeJobProvider = analyzeJobProvider;
        this.analysisRunner = analysisRunner;
    }

    @Transactional
    @Scheduled(cron = "${application.schedules.spending.analyze.cron}")
    void analyzeMonthlySpendingPatterns() {
        MDC.put("correlationId", UUID.randomUUID().toString());
        var jobToRun = analyzeJobProvider.first();
        if (jobToRun.isPresent()) {
            var analyzeJob = jobToRun.get();
            log.info("Scheduling analyze job {}.", analyzeJob.getMonth());
            InternalAuthenticationEvent.authenticate(analyzeJob.getUser().email());
            var success = analysisRunner.analyzeForUser(analyzeJob.getMonth());

            if (success) {
                log.debug(
                        "Completed analysis for month {} for user {}.",
                        analyzeJob.getMonth(),
                        analyzeJob.getUser().email());
                analyzeJob.complete();
            } else {
                log.warn(
                        "Failed to complete analysis for month {} for user {}.",
                        analyzeJob.getMonth(),
                        analyzeJob.getUser().email());
                analyzeJob.fail();
            }
        }
        MDC.remove("correlationId");
    }
}
