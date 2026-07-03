package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;

import java.util.HashMap;
import java.util.Optional;

public class UnusualAmount implements Anomaly {

    private static final double MIN_STD_DEV = 0.01;
    private static final int MIN_TRANSACTIONS = 5;

    private final SpendingAnalysisConfiguration settings;

    public UnusualAmount(SpendingAnalysisConfiguration settings) {
        this.settings = settings;
    }

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var typicalAmount = statistics.amounts().get(getExpense(transaction));
        if (typicalAmount == null) {
            return Optional.empty();
        }

        double mean = typicalAmount.getMean();
        double stdDev = typicalAmount.getStandardDeviation();

        if (typicalAmount.getN() < MIN_TRANSACTIONS || stdDev < MIN_STD_DEV) {
            return Optional.empty();
        }

        var transactionAmount = transaction.computeAmount(transaction.computeTo());
        var zScore = Math.abs(transactionAmount - mean) / stdDev;
        var adjustedThreshold = settings.adjustedAmountThreshold();

        if (zScore > adjustedThreshold) {
            var score = Math.min(1.0, zScore / (adjustedThreshold * 2));
            var metadata = new HashMap<String, Object>();
            metadata.put("amount", transactionAmount);
            metadata.put("z_score", zScore);
            metadata.put("mean", mean);
            metadata.put("std_dev", stdDev);
            metadata.put("baseline_months", statistics.baselineMonths());
            metadata.put("direction", transactionAmount > mean ? "UP" : "DOWN");

            return Optional.of(new SpendingInsight(
                    InsightType.UNUSUAL_AMOUNT,
                    getExpense(transaction),
                    getSeverityFromScore(score),
                    score,
                    transaction.getId(),
                    transaction.getDate(),
                    generateMessage(transactionAmount, mean),
                    metadata));
        }

        return Optional.empty();
    }

    private String generateMessage(double transactionAmount, double mean) {
        if (transactionAmount > mean) {
            return "computed.insight.amount.high";
        }
        return "computed.insight.amount.low";
    }
}
