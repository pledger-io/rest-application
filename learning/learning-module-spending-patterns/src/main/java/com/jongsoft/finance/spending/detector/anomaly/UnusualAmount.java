package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;

import java.util.Map;
import java.util.Optional;

public class UnusualAmount implements Anomaly {
    private static final double AMOUNT_ANOMALY_THRESHOLD = 2.0;
    private static final double SENSITIVITY = 0.15;

    @Override
    public Optional<SpendingInsight> detect(
            Transaction transaction, UserCategoryStatistics statistics) {
        var typicalAmount = statistics.amounts().get(transaction.getBudget());
        if (typicalAmount == null) {
            return Optional.empty();
        }

        double mean = typicalAmount.getMean();
        double stdDev = typicalAmount.getStandardDeviation();

        // Skip if we don't have enough data or standard deviation is too small
        if (typicalAmount.getN() < 5 || stdDev < 0.01) {
            return Optional.empty();
        }

        var transactionAmount = transaction.computeAmount(transaction.computeTo());
        var zScore = Math.abs(transactionAmount - mean) / stdDev;
        var adjustedThreshold = AMOUNT_ANOMALY_THRESHOLD * (2.0 - SENSITIVITY);

        if (zScore > adjustedThreshold) {
            var score = Math.min(1.0, zScore / (adjustedThreshold * 2));

            return Optional.of(
                    SpendingInsight.builder()
                            .type(InsightType.UNUSUAL_AMOUNT)
                            .category(transaction.getBudget())
                            .severity(getSeverityFromScore(score))
                            .score(score)
                            .detectedDate(transaction.getDate())
                            .message(generateMessage(transactionAmount, mean))
                            .transactionId(transaction.getId())
                            .metadata(
                                    Map.of(
                                            "amount", transactionAmount,
                                            "z_score", zScore,
                                            "mean", mean,
                                            "std_dev", stdDev))
                            .build());
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
