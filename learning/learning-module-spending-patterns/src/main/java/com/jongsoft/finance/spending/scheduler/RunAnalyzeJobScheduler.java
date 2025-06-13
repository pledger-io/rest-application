package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.AnalyzeJobProvider;
import com.jongsoft.finance.providers.UserProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

@Slf4j
@Singleton
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
    var jobToRun = analyzeJobProvider.first();
    if (jobToRun.isPresent()) {
      var analyzeJob = jobToRun.get();
      log.info("Scheduling analyze job {}.", analyzeJob.getMonth());
      for (var user : userProvider.lookup()) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        eventPublisher.publishEvent(new InternalAuthenticationEvent(this, user.getUsername().email()));
        analysisRunner.analyzeForUser(analyzeJob.getMonth());
        MDC.remove("correlationId");
      }
      analyzeJob.complete();
    }
  }

}
