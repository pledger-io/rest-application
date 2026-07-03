package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static org.junit.jupiter.api.Assertions.*;

import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Unusual Frequency")
class UnusualFrequencyTest {
    private final SpendingAnalysisConfiguration configuration = new SpendingAnalysisConfiguration();

    @Test
    @DisplayName("Should detect unusually high frequency")
    void shouldDetectUnusuallyHighFrequency() {
        UnusualFrequency unusualFrequency = new UnusualFrequency(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(1000.0, 10, java.util.List.of());

        UserCategoryStatistics statistics =
                createStatisticsWithFrequencies("Groceries", new double[] {4.0, 5.0, 3.0});

        Optional<SpendingInsight> result =
                unusualFrequency.detect("Groceries", month, summary, statistics);

        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Groceries", insight.getCategory());
        assertEquals("computed.insight.frequency.high", insight.getMessage());
        assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal());
        assertNull(insight.getTransactionId());
        assertEquals("UP", insight.getMetadata().get("direction"));
    }

    @Test
    @DisplayName("Should detect unusually low frequency")
    void shouldDetectUnusuallyLowFrequency() {
        UnusualFrequency unusualFrequency = new UnusualFrequency(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(50.0, 1, java.util.List.of());

        UserCategoryStatistics statistics =
                createStatisticsWithFrequencies("Utilities", new double[] {5.0, 6.0, 5.0});

        Optional<SpendingInsight> result =
                unusualFrequency.detect("Utilities", month, summary, statistics);

        assertTrue(result.isPresent());
        assertEquals("computed.insight.frequency.low", result.get().getMessage());
        assertEquals("DOWN", result.get().getMetadata().get("direction"));
    }

    @Test
    @DisplayName("Do not detect unusually high frequency when within normal range")
    void shouldNotDetectUnusualFrequencyWhenWithinNormalRange() {
        UnusualFrequency unusualFrequency = new UnusualFrequency(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(100.0, 3, java.util.List.of());

        UserCategoryStatistics statistics =
                createStatisticsWithFrequencies("Entertainment", new double[] {2.0, 3.0, 4.0});

        assertFalse(unusualFrequency
                .detect("Entertainment", month, summary, statistics)
                .isPresent());
    }

    @Test
    @DisplayName("Handle missing statistics gracefully")
    void shouldHandleMissingStatistics() {
        UnusualFrequency unusualFrequency = new UnusualFrequency(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(100.0, 3, java.util.List.of());
        UserCategoryStatistics statistics = new UserCategoryStatistics(12);

        assertFalse(
                unusualFrequency.detect("Travel", month, summary, statistics).isPresent());
    }

    @Test
    @DisplayName("Handle insufficient data gracefully")
    void shouldHandleInsufficientData() {
        UnusualFrequency unusualFrequency = new UnusualFrequency(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(100.0, 3, java.util.List.of());

        UserCategoryStatistics statistics =
                createStatisticsWithFrequencies("Dining", new double[] {2.0, 3.0});

        assertFalse(
                unusualFrequency.detect("Dining", month, summary, statistics).isPresent());
    }

    private UserCategoryStatistics createStatisticsWithFrequencies(
            String category, double[] monthlyCounts) {
        UserCategoryStatistics statistics = new UserCategoryStatistics(12);
        DescriptiveStatistics frequencies = new DescriptiveStatistics();
        for (double count : monthlyCounts) {
            frequencies.addValue(count);
        }
        statistics.frequencies().put(category, frequencies);
        return statistics;
    }
}
