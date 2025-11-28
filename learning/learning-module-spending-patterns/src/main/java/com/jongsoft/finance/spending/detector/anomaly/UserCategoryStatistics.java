package com.jongsoft.finance.spending.detector.anomaly;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record UserCategoryStatistics(
        BudgetStatisticsMap amounts,
        BudgetStatisticsMap frequencies,
        Map<String, Set<String>> typicalMerchants) {
    public static class BudgetStatisticsMap extends HashMap<String, DescriptiveStatistics> {}
}
