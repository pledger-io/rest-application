package com.jongsoft.finance.spending.detector.anomaly;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public record UserCategoryStatistics(
    BudgetStatisticsMap amounts,
    BudgetStatisticsMap frequencies,
    Map<String, Set<String>> typicalMerchants) {
  public static class BudgetStatisticsMap extends HashMap<String, DescriptiveStatistics> {}
}
