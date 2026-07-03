package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record UserCategoryStatistics(
        BudgetStatisticsMap amounts,
        BudgetStatisticsMap frequencies,
        BudgetStatisticsMap monthlyTotals,
        Map<String, Set<String>> typicalMerchants,
        Map<String, Map<String, Double>> monthlyTotalsByMonthKey,
        int baselineMonths) {

    public UserCategoryStatistics(int baselineMonths) {
        this(
                new BudgetStatisticsMap(),
                new BudgetStatisticsMap(),
                new BudgetStatisticsMap(),
                new HashMap<>(),
                new HashMap<>(),
                baselineMonths);
    }

    public static class BudgetStatisticsMap extends HashMap<String, DescriptiveStatistics> {}
}
