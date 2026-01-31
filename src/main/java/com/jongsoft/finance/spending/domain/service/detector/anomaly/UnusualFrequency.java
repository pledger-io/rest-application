package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Dates;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

public class UnusualFrequency implements Anomaly {

    private static final double FREQUENCY_ANOMALY_THRESHOLD = 1.5;
    private static final double ADJUSTED_THRESHOLD = FREQUENCY_ANOMALY_THRESHOLD * (2.0 - .7);

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UnusualFrequency.class);

    private final BudgetProvider budgetProvider;
    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterFactory;

    public UnusualFrequency(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            BudgetProvider budgetProvider) {
        this.transactionProvider = transactionProvider;
        this.filterFactory = filterFactory;
        this.budgetProvider = budgetProvider;
    }

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var typicalFrequency = statistics.frequencies().get(getExpense(transaction));
        if (typicalFrequency == null || typicalFrequency.getN() < 3) {
            log.trace(
                    "Not enough data for transaction {}. Skipping anomaly detection.",
                    transaction.getId());
            return Optional.empty();
        }

        long currentMonthCount = computeTransactionsInMonth(transaction);
        double mean = typicalFrequency.getMean();
        double stdDev = typicalFrequency.getStandardDeviation();
        double zScore = Math.abs(currentMonthCount - mean) / stdDev;

        if (zScore > ADJUSTED_THRESHOLD) {
            double score = Math.min(1.0, zScore / (ADJUSTED_THRESHOLD * 2));

            return Optional.of(new SpendingInsight(
                    InsightType.UNUSUAL_FREQUENCY,
                    getExpense(transaction),
                    getSeverityFromScore(score),
                    score,
                    transaction.getId(),
                    transaction.getDate(),
                    generateMessage(currentMonthCount, mean),
                    Map.of(
                            "frequency", currentMonthCount,
                            "z_score", zScore,
                            "mean", mean,
                            "std_dev", stdDev)));
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
        var expense = budgetProvider
                .lookup(transaction.getDate().getYear(), transaction.getDate().getMonthValue())
                .stream()
                .flatMap(b -> b.getExpenses().stream())
                .filter(e -> e.getName().equalsIgnoreCase(getExpense(transaction)))
                .findFirst()
                .orElseThrow();

        var filter = filterFactory
                .create()
                .expenses(Collections.List(new EntityRef(expense.getId())))
                .range(Dates.range(transaction.getDate().withDayOfMonth(1), ChronoUnit.MONTHS))
                .page(1, 1);

        return transactionProvider.lookup(filter).total();
    }
}
