package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Detects category-level spending anomalies for a complete month. */
public interface MonthAnomaly {

    Optional<SpendingInsight> detect(
            String category,
            YearMonth forMonth,
            CategoryMonthSummary summary,
            UserCategoryStatistics statistics);

    default Severity getSeverityFromScore(double score) {
        if (score >= 0.8) {
            return Severity.ALERT;
        } else if (score >= 0.5) {
            return Severity.WARNING;
        }
        return Severity.INFO;
    }

    default Map<String, Object> baselineMetadata(UserCategoryStatistics statistics) {
        var metadata = new HashMap<String, Object>();
        metadata.put("baseline_months", statistics.baselineMonths());
        return metadata;
    }
}
