package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.InsightType;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Detects when a category's monthly spending total deviates significantly from its historic
 * monthly average.
 */
public class CategoryMonthlyDeviation implements MonthAnomaly {

    private static final double MIN_STD_DEV = 0.01;
    private static final int MIN_BASELINE_MONTHS = 3;

    private final SpendingAnalysisConfiguration settings;

    public CategoryMonthlyDeviation(SpendingAnalysisConfiguration settings) {
        this.settings = settings;
    }

    @Override
    public Optional<SpendingInsight> detect(
            String category,
            YearMonth forMonth,
            CategoryMonthSummary summary,
            UserCategoryStatistics statistics) {
        var historicMonthlyTotals = statistics.monthlyTotals().get(category);
        if (historicMonthlyTotals == null || historicMonthlyTotals.getN() < MIN_BASELINE_MONTHS) {
            return Optional.empty();
        }

        double historicMean = historicMonthlyTotals.getMean();
        double stdDev = historicMonthlyTotals.getStandardDeviation();
        if (stdDev < MIN_STD_DEV) {
            return Optional.empty();
        }

        double currentTotal = summary.totalAmount();
        double zScore = Math.abs(currentTotal - historicMean) / stdDev;
        double threshold = settings.adjustedMonthlyTotalThreshold();

        if (zScore <= threshold) {
            return Optional.empty();
        }

        double score = Math.min(1.0, zScore / (threshold * 2));
        double percentChange = historicMean == 0.0
                ? (currentTotal == 0.0 ? 0.0 : 100.0)
                : ((currentTotal - historicMean) / historicMean) * 100.0;
        String direction = currentTotal > historicMean ? "UP" : "DOWN";

        var metadata = new HashMap<>(baselineMetadata(statistics));
        metadata.put("current_month_total", currentTotal);
        metadata.put("historic_mean", historicMean);
        metadata.put("historic_std_dev", stdDev);
        metadata.put("percent_change", percentChange);
        metadata.put("z_score", zScore);
        metadata.put("direction", direction);
        metadata.put("top_contributing_transactions", summary.topContributingTransactionIds());
        addComparisonMetadata(category, forMonth, statistics, metadata);

        return Optional.of(new SpendingInsight(
                InsightType.SPENDING_SPIKE,
                category,
                getSeverityFromScore(score),
                score,
                null,
                forMonth.atDay(1),
                "computed.insight.spending.spike",
                metadata));
    }

    private void addComparisonMetadata(
            String category,
            YearMonth forMonth,
            UserCategoryStatistics statistics,
            Map<String, Object> metadata) {
        var monthlyHistory = statistics.monthlyTotalsByMonthKey().get(category);
        if (monthlyHistory == null) {
            return;
        }

        Double previousTotal = monthlyHistory.get(monthKey(forMonth.minusMonths(1)));
        Double lastYearTotal = monthlyHistory.get(monthKey(forMonth.minusYears(1)));

        if (previousTotal != null) {
            metadata.put("previous_month_total", previousTotal);
        }
        if (lastYearTotal != null) {
            metadata.put("same_month_last_year_total", lastYearTotal);
        }
    }

    private static String monthKey(YearMonth month) {
        return month.getYear() + "-" + month.getMonthValue();
    }
}
