package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnusualFrequency implements Anomaly {

  private static final double FREQUENCY_ANOMALY_THRESHOLD = 1.5;
  private static final double ADJUSTED_THRESHOLD = FREQUENCY_ANOMALY_THRESHOLD * (2.0 - .7);

  private final BudgetProvider budgetProvider;
  private final TransactionProvider transactionProvider;
  private final FilterFactory filterFactory;

  public UnusualFrequency(
      TransactionProvider transactionProvider,
      FilterFactory filterFactory,
      BudgetProvider budgetProvider) {
    this.transactionProvider = transactionProvider;
    this.filterFactory = filterFactory;
    this.budgetProvider = budgetProvider;
  }

  @Override
  public Optional<SpendingInsight> detect(
      Transaction transaction, UserCategoryStatistics statistics) {
    var typicalFrequency = statistics.frequencies().get(transaction.getBudget());
    if (typicalFrequency == null || typicalFrequency.getN() < 3) {
      log.trace(
          "Not enough data for transaction {}. Skipping anomaly detection.", transaction.getId());
      return Optional.empty();
    }

    long currentMonthCount = computeTransactionsInMonth(transaction);
    double mean = typicalFrequency.getMean();
    double stdDev = typicalFrequency.getStandardDeviation();
    double zScore = Math.abs(currentMonthCount - mean) / stdDev;

    if (zScore > ADJUSTED_THRESHOLD) {
      double score = Math.min(1.0, zScore / (ADJUSTED_THRESHOLD * 2));

      return Optional.of(
          SpendingInsight.builder()
              .type(InsightType.UNUSUAL_FREQUENCY)
              .category(transaction.getBudget())
              .severity(getSeverityFromScore(score))
              .score(score)
              .detectedDate(transaction.getDate())
              .message(generateMessage(currentMonthCount, mean))
              .transactionId(transaction.getId())
              .metadata(
                  Map.of(
                      "frequency", currentMonthCount,
                      "z_score", zScore,
                      "mean", mean,
                      "std_dev", stdDev))
              .build());
    }

    return Optional.empty();
  }

  private String generateMessage(long currentMonthCount, double mean) {
    if (currentMonthCount > mean) {
      return "computed.insight.frequency.high";
    }
    return "computed.insight.frequency.low";
  }

  protected long computeTransactionsInMonth(Transaction transaction) {
    var expense =
        budgetProvider
            .lookup(transaction.getDate().getYear(), transaction.getDate().getMonthValue())
            .stream()
            .flatMap(b -> b.getExpenses().stream())
            .filter(e -> e.getName().equalsIgnoreCase(transaction.getBudget()))
            .findFirst()
            .orElseThrow();

    var filter =
        filterFactory
            .transaction()
            .expenses(Collections.List(new EntityRef(expense.getId())))
            .range(Dates.range(transaction.getDate().withDayOfMonth(1), ChronoUnit.MONTHS))
            .page(1, 1);

    return transactionProvider.lookup(filter).total();
  }
}
