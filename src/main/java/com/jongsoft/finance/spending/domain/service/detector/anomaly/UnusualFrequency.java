package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Optional;

/** Detects when the number of transactions in a category for the analyzed month is unusual. */
public class UnusualFrequency implements MonthAnomaly {

    private static final int MIN_BASELINE_MONTHS = 3;
    private static final double MIN_STD_DEV = 0.01;

    private final SpendingAnalysisConfiguration settings;

    public UnusualFrequency(SpendingAnalysisConfiguration settings) {
        this.settings = settings;
    }

    @Override
    public Optional<SpendingInsight> detect(
            String category,
            YearMonth forMonth,
            CategoryMonthSummary summary,
            UserCategoryStatistics statistics) {
        var typicalFrequency = statistics.frequencies().get(category);
        if (typicalFrequency == null || typicalFrequency.getN() < MIN_BASELINE_MONTHS) {
            return Optional.empty();
        }

        long currentMonthCount = summary.transactionCount();
        double mean = typicalFrequency.getMean();
        double stdDev = typicalFrequency.getStandardDeviation();
        if (stdDev < MIN_STD_DEV) {
            return Optional.empty();
        }

        double zScore = Math.abs(currentMonthCount - mean) / stdDev;
        double threshold = settings.adjustedFrequencyThreshold();

        if (zScore <= threshold) {
            return Optional.empty();
        }

        double score = Math.min(1.0, zScore / (threshold * 2));
        String direction = currentMonthCount > mean ? "UP" : "DOWN";

        var metadata = new HashMap<>(baselineMetadata(statistics));
        metadata.put("frequency", currentMonthCount);
        metadata.put("z_score", zScore);
        metadata.put("mean", mean);
        metadata.put("std_dev", stdDev);
        metadata.put("direction", direction);

        return Optional.of(new SpendingInsight(
                InsightType.UNUSUAL_FREQUENCY,
                category,
                getSeverityFromScore(score),
                score,
                null,
                forMonth.atDay(1),
                generateMessage(currentMonthCount, mean),
                metadata));
    }

    private String generateMessage(long currentMonthCount, double mean) {
        if (currentMonthCount > mean) {
            return "computed.insight.frequency.high";
        }
        return "computed.insight.frequency.low";
    }
}
