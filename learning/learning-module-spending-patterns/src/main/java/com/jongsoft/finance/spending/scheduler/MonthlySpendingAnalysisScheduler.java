package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.domain.insight.AnalyzeJob;
import com.jongsoft.finance.messaging.InternalAuthenticationEvent;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
public class MonthlySpendingAnalysisScheduler {

  private final UserProvider userProvider;
  private final ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher;

  public MonthlySpendingAnalysisScheduler(
      UserProvider userProvider,
      ApplicationEventPublisher<InternalAuthenticationEvent> eventPublisher) {
    this.userProvider = userProvider;
    this.eventPublisher = eventPublisher;
  }

  @Scheduled(cron = "0 0 0 15 1-12 *")
  public void analyzeMonthlySpendingPatterns() {
    log.info("Scheduling monthly spending analysis, for month {}.", YearMonth.now());
    for (var user : userProvider.lookup()) {
      eventPublisher.publishEvent(
          new InternalAuthenticationEvent(this, user.getUsername().email()));
      new AnalyzeJob(user.getUsername(), YearMonth.now());
    }
  }
}
