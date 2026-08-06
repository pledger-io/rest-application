package com.jongsoft.finance.spending.domain.service.detector.anomaly.data;

import java.time.YearMonth;

/** Detects category-level spending anomalies for a complete month. */
public record MonthAnomalyData(String category, YearMonth forMonth, CategoryMonthSummary summary) {}
