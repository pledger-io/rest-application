package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.learning.UserScopedExecutor;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.Detector;
import com.jongsoft.lang.Dates;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Singleton
public class MonthlySpendingAnalysisScheduler {

  private final UserScopedExecutor userScopedExecutor;
  private final List<Detector<?>> transactionDetectors;

  private final FilterFactory filterFactory;
  private final TransactionProvider transactionProvider;

  public MonthlySpendingAnalysisScheduler(
      UserScopedExecutor userScopedExecutor,
      List<Detector<?>> transactionDetectors,
      FilterFactory filterFactory,
      TransactionProvider transactionProvider) {
    this.userScopedExecutor = userScopedExecutor;
    this.transactionDetectors = transactionDetectors;
    this.filterFactory = filterFactory;
    this.transactionProvider = transactionProvider;
  }

  //@Scheduled(cron = "0 0 2 1 * *")
  @Scheduled(initialDelay = "2s")
  public void analyzeMonthlySpendingPatterns() {
    log.info("Starting monthly spending analysis...");
    userScopedExecutor.runForPerUser(this::analyzeForUser);
  }

  private void analyzeForUser() {
    YearMonth previousMonth = YearMonth.now().minusMonths(1);
    log.info("Starting monthly spending analysis for {}.", previousMonth);

    var transactionFilter = filterFactory.transaction()
        .range(Dates.range(previousMonth.atDay(1), previousMonth.atEndOfMonth()));

    try {
      var transactionInMonth = transactionProvider.lookup(transactionFilter).content();
      transactionDetectors.forEach(Detector::updateBaseline);
      transactionInMonth
          .stream()
          .flatMap(t -> processTransaction(t).stream())
          .distinct()
          .peek(insight -> log.info("Insight: {}", insight.toString()))
          .forEach(Insight::signal);
      log.debug("Completed monthly spending analysis for {}.", previousMonth);
    } catch (Exception e) {
      log.error("Failed to analyze monthly spending patterns for {}.", previousMonth, e);
    }
  }

  private List<? extends Insight> processTransaction(Transaction transaction) {
    if (transaction.getCategory() == null && transaction.getBudget() == null) {
      return List.of(); // Skip transactions without a category
    }

    var insightList = new java.util.ArrayList<Insight>();
    for (var detector : transactionDetectors) {
      log.trace("Processing transaction {} with detector {}.", transaction, detector);
      insightList.addAll(detector.detect(transaction));
    }
    return insightList;
  }
}
