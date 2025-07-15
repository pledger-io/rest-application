package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.AnalyzeJobProvider;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
public class RunAnalyzeJobScheduler {

  private final AnalyzeJobProvider analyzeJobProvider;
  private final AnalysisRunner analysisRunner;
  private final UserProvider userProvider;
  private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

  RunAnalyzeJobScheduler(
      AnalyzeJobProvider analyzeJobProvider,
      AnalysisRunner analysisRunner,
      UserProvider userProvider,
      ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher) {
    this.analyzeJobProvider = analyzeJobProvider;
    this.analysisRunner = analysisRunner;
    this.userProvider = userProvider;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  @Scheduled(cron = "*/5 * * * * *")
  public void analyzeMonthlySpendingPatterns() {
    MDC.put("correlationId", UUID.randomUUID().toString());
    var jobToRun = analyzeJobProvider.first();
    if (jobToRun.isPresent()) {
      var analyzeJob = jobToRun.get();
      log.info("Scheduling analyze job {}.", analyzeJob.getMonth());
      eventPublisher.publishEvent(
          new InternalAuthenticationEvent(this, analyzeJob.getUser().email()));
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
