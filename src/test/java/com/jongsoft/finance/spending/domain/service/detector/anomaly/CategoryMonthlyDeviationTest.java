package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Transaction;
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
@DisplayName("Unit - Category Monthly Deviation")
class CategoryMonthlyDeviationTest {

    private final SpendingAnalysisConfiguration configuration = new SpendingAnalysisConfiguration();

    @Test
    @DisplayName("Detect spending spike when monthly total exceeds historic mean")
    void shouldDetectSpendingSpikeWhenCurrentMonthSpendingExceedsThreshold() {
        CategoryMonthlyDeviation detector = new CategoryMonthlyDeviation(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(500.0, 4, java.util.List.of(1L));

        UserCategoryStatistics statistics = createStatisticsWithMonthlyTotals(
                "Food", new double[] {200.0, 250.0, 300.0, 280.0});

        Optional<SpendingInsight> result = detector.detect("Food", month, summary, statistics);

        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Food", insight.getCategory());
        assertEquals("computed.insight.spending.spike", insight.getMessage());
        assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal());
        assertNull(insight.getTransactionId());
        assertEquals("UP", insight.getMetadata().get("direction"));
    }

    @Test
    @DisplayName("Do not detect spending spike when within normal range")
    void shouldNotDetectSpendingSpikeWhenCurrentMonthSpendingWithinThreshold() {
        CategoryMonthlyDeviation detector = new CategoryMonthlyDeviation(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(160.0, 2, java.util.List.of(1L));

        UserCategoryStatistics statistics = createStatisticsWithMonthlyTotals(
                "Utilities", new double[] {100.0, 120.0, 150.0, 140.0});

        assertTrue(detector.detect("Utilities", month, summary, statistics).isEmpty());
    }

    @Test
    @DisplayName("Handle insufficient monthly spending data gracefully")
    void shouldHandleEmptyMonthlySpendingDataGracefully() {
        CategoryMonthlyDeviation detector = new CategoryMonthlyDeviation(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(500.0, 1, java.util.List.of(1L));
        UserCategoryStatistics statistics = new UserCategoryStatistics(12);

        assertTrue(detector.detect("Travel", month, summary, statistics).isEmpty());
    }

    @Test
    @DisplayName("Detect spending spike with edge case values")
    void shouldDetectSpendingSpikeWithEdgeCaseValues() {
        CategoryMonthlyDeviation detector = new CategoryMonthlyDeviation(configuration);
        YearMonth month = YearMonth.of(2025, 5);
        CategoryMonthSummary summary = new CategoryMonthSummary(200.0, 1, java.util.List.of(1L));

        UserCategoryStatistics statistics =
                createStatisticsWithMonthlyTotals("Entertainment", new double[] {10.0, 12.0, 11.0});

        Optional<SpendingInsight> result =
                detector.detect("Entertainment", month, summary, statistics);

        assertTrue(result.isPresent());
        assertEquals(Severity.ALERT, result.get().getSeverity());
    }

    @Test
    @DisplayName("Build category summary from transactions")
    void shouldBuildCategorySummaryFromTransactions() {
        Transaction transaction = mock(Transaction.class);
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(150.0);
        when(transaction.getId()).thenReturn(42L);

        CategoryMonthSummary summary = CategoryMonthSummary.from(java.util.List.of(transaction));

        assertEquals(150.0, summary.totalAmount());
        assertEquals(1, summary.transactionCount());
        assertEquals(42L, summary.topContributingTransactionIds().getFirst());
    }

    private UserCategoryStatistics createStatisticsWithMonthlyTotals(
            String category, double[] monthlyValues) {
        UserCategoryStatistics statistics = new UserCategoryStatistics(12);
        DescriptiveStatistics monthlyTotals = new DescriptiveStatistics();
        for (double value : monthlyValues) {
            monthlyTotals.addValue(value);
        }
        statistics.monthlyTotals().put(category, monthlyTotals);
        statistics.amounts().put(category, monthlyTotals);
        return statistics;
    }
}
