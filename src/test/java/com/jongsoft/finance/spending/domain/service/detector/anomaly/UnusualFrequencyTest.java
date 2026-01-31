package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Test subclass of UnusualFrequency that allows us to override the private computeTransactionsInMonth method
 */
class TestableUnusualFrequency extends UnusualFrequency {
    private long transactionsInMonth;

    public TestableUnusualFrequency(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterFactory,
            BudgetProvider budgetProvider,
            long transactionsInMonth) {
        super(transactionProvider, filterFactory, budgetProvider);
        this.transactionsInMonth = transactionsInMonth;
    }

    @Override
    protected long computeTransactionsInMonth(Transaction transaction) {
        return transactionsInMonth;
    }
}

@Tag("unit")
@DisplayName("Unit - Unusual Frequency")
class UnusualFrequencyTest {

    /**
     * This test class validates the `detect` method in UnusualFrequency.
     * The `detect` method identifies transactions with unusually high or low frequency
     * compared to the typical frequency for a budget category.
     */
    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @Test
    @DisplayName("Should detect unusually high frequency")
    void shouldDetectUnusuallyHighFrequency() {
        // Arrange
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);

        // Create a testable instance with high frequency (10 transactions)
        TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
                transactionProvider, filterFactory, budgetProvider, 10L);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getId()).thenReturn(123L);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        UserCategoryStatistics.BudgetStatisticsMap frequenciesMap =
                new UserCategoryStatistics.BudgetStatisticsMap();

        DescriptiveStatistics groceriesStats = new DescriptiveStatistics();
        // Add typical frequencies for groceries (transactions per month)
        groceriesStats.addValue(4.0);
        groceriesStats.addValue(5.0);
        groceriesStats.addValue(3.0);
        frequenciesMap.put("Groceries", groceriesStats);

        when(statistics.frequencies()).thenReturn(frequenciesMap);

        // Act
        Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

        // Assert
        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Groceries", insight.getCategory());
        assertEquals("computed.insight.frequency.high", insight.getMessage());
        assertTrue(insight.getSeverity().ordinal()
                >= Severity.WARNING.ordinal()); // Should be at least WARNING
        assertTrue(insight.getMetadata().containsKey("frequency"));
        assertTrue(insight.getMetadata().containsKey("z_score"));
        assertTrue(insight.getMetadata().containsKey("mean"));
        assertTrue(insight.getMetadata().containsKey("std_dev"));
    }

    @Test
    @DisplayName("Should detect unusually low frequency")
    void shouldDetectUnusuallyLowFrequency() {
        // Arrange
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);

        // Create a testable instance with low frequency (1 transaction)
        TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
                transactionProvider, filterFactory, budgetProvider, 1L);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Utilities")).when(transaction).getMetadata();
        when(transaction.getId()).thenReturn(456L);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        UserCategoryStatistics.BudgetStatisticsMap frequenciesMap =
                new UserCategoryStatistics.BudgetStatisticsMap();

        DescriptiveStatistics utilitiesStats = new DescriptiveStatistics();
        // Add typical frequencies for utilities (transactions per month)
        utilitiesStats.addValue(5.0);
        utilitiesStats.addValue(6.0);
        utilitiesStats.addValue(5.0);
        frequenciesMap.put("Utilities", utilitiesStats);

        when(statistics.frequencies()).thenReturn(frequenciesMap);

        // Act
        Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

        // Assert
        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Utilities", insight.getCategory());
        assertEquals("computed.insight.frequency.low", insight.getMessage());
        assertTrue(insight.getSeverity().ordinal()
                >= Severity.WARNING.ordinal()); // Should be at least WARNING
    }

    @Test
    @DisplayName("Do not detect unusually high frequency when within normal range")
    void shouldNotDetectUnusualFrequencyWhenWithinNormalRange() {
        // Arrange
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);

        // Create a testable instance with normal frequency (3 transactions)
        TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
                transactionProvider, filterFactory, budgetProvider, 3L);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Entertainment")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        UserCategoryStatistics.BudgetStatisticsMap frequenciesMap =
                new UserCategoryStatistics.BudgetStatisticsMap();

        DescriptiveStatistics entertainmentStats = new DescriptiveStatistics();
        // Add typical frequencies for entertainment (transactions per month)
        entertainmentStats.addValue(2.0);
        entertainmentStats.addValue(3.0);
        entertainmentStats.addValue(4.0);
        frequenciesMap.put("Entertainment", entertainmentStats);

        when(statistics.frequencies()).thenReturn(frequenciesMap);

        // Act
        Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle null statistics gracefully")
    void shouldHandleNullStatistics() {
        // Arrange
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);

        UnusualFrequency unusualFrequency =
                new UnusualFrequency(transactionProvider, filterFactory, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Travel")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        UserCategoryStatistics.BudgetStatisticsMap frequenciesMap =
                new UserCategoryStatistics.BudgetStatisticsMap();
        when(statistics.frequencies()).thenReturn(frequenciesMap);

        // Act
        Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle insufficient data gracefully")
    void shouldHandleInsufficientData() {
        // Arrange
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);

        UnusualFrequency unusualFrequency =
                new UnusualFrequency(transactionProvider, filterFactory, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Dining")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        UserCategoryStatistics.BudgetStatisticsMap frequenciesMap =
                new UserCategoryStatistics.BudgetStatisticsMap();

        DescriptiveStatistics diningStats = new DescriptiveStatistics();
        // Add only a few data points (less than 3)
        diningStats.addValue(2.0);
        diningStats.addValue(3.0);
        frequenciesMap.put("Dining", diningStats);

        when(statistics.frequencies()).thenReturn(frequenciesMap);

        // Act
        Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }
}
