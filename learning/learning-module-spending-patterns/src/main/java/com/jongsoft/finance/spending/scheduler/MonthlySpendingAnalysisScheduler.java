package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.domain.insight.AnalyzeJob;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MonthlySpendingAnalysisScheduler {

  @Scheduled(cron = "0 0 0 15 1-12 *")
  public void analyzeMonthlySpendingPatterns() {
    log.info("Scheduling monthly spending analysis, for month {}.", YearMonth.now());
    new AnalyzeJob(YearMonth.now());
  }
}
