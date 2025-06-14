package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.Detector;
import com.jongsoft.lang.Dates;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
class AnalysisRunner {
  private final List<Detector<?>> transactionDetectors;
  private final FilterFactory filterFactory;
  private final TransactionProvider transactionProvider;

  AnalysisRunner(
      List<Detector<?>> transactionDetectors,
      FilterFactory filterFactory,
      TransactionProvider transactionProvider) {
    this.transactionDetectors = transactionDetectors;
    this.filterFactory = filterFactory;
    this.transactionProvider = transactionProvider;
  }

  @Transactional
  public boolean analyzeForUser(YearMonth month) {
    if (!transactionDetectors.stream().allMatch(Detector::readyForAnalysis)) {
      return false;
    }

    log.info("Starting monthly spending analysis for {}.", month);
    try {
      var transactionFilter =
          filterFactory.transaction().range(Dates.range(month.atDay(1), month.atEndOfMonth()));

      var transactionInMonth = transactionProvider.lookup(transactionFilter).content();
      if (transactionInMonth.isEmpty()) {
        log.trace("No transactions found for {}, skipping analysis.", month);
        return false;
      }

      log.debug("Retrieved {} transactions for {}.", transactionInMonth.size(), month);
      transactionDetectors.forEach(detector -> detector.updateBaseline(month));
      transactionInMonth.stream()
          .flatMap(t -> processTransaction(t).stream())
          .distinct()
          .forEach(Insight::signal);
      log.debug("Completed monthly spending analysis for {}.", month);
    } catch (Exception e) {
      log.error("Error occurred while processing monthly spending analysis for {}.", month, e);
      return false;
    }

    return true;
  }

  private List<? extends Insight> processTransaction(Transaction transaction) {
    if (transaction.getCategory() == null && transaction.getBudget() == null) {
      return List.of(); // Skip transactions without a category
    }

    var insightList = new java.util.ArrayList<Insight>();
    for (var detector : transactionDetectors) {
      log.trace(
          "Processing transaction {} with detector {}.",
          transaction,
          detector.getClass().getSimpleName());
      insightList.addAll(detector.detect(transaction));
    }
    return insightList;
  }
}
