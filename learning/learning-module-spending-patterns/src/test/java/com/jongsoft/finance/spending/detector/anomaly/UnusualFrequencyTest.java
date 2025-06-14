package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test subclass of UnusualFrequency that allows us to override the private computeTransactionsInMonth method
 */
class TestableUnusualFrequency extends UnusualFrequency {
  private long transactionsInMonth;

  public TestableUnusualFrequency(TransactionProvider transactionProvider,
                                  FilterFactory filterFactory,
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

class UnusualFrequencyTest {

  /**
   * This test class validates the `detect` method in UnusualFrequency.
   * The `detect` method identifies transactions with unusually high or low frequency
   * compared to the typical frequency for a budget category.
   */

  @Test
  void shouldDetectUnusuallyHighFrequency() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    // Create a testable instance with high frequency (10 transactions)
    TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
        transactionProvider, filterFactory, budgetProvider, 10L);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Groceries");
    when(transaction.getId()).thenReturn(123L);
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap frequenciesMap = new UserCategoryStatistics.BudgetStatisticsMap();

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
    assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal()); // Should be at least WARNING
    assertTrue(insight.getMetadata().containsKey("frequency"));
    assertTrue(insight.getMetadata().containsKey("z_score"));
    assertTrue(insight.getMetadata().containsKey("mean"));
    assertTrue(insight.getMetadata().containsKey("std_dev"));
  }

  @Test
  void shouldDetectUnusuallyLowFrequency() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    // Create a testable instance with low frequency (1 transaction)
    TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
        transactionProvider, filterFactory, budgetProvider, 1L);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Utilities");
    when(transaction.getId()).thenReturn(456L);
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap frequenciesMap = new UserCategoryStatistics.BudgetStatisticsMap();

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
    assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal()); // Should be at least WARNING
  }

  @Test
  void shouldNotDetectUnusualFrequencyWhenWithinNormalRange() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    // Create a testable instance with normal frequency (3 transactions)
    TestableUnusualFrequency unusualFrequency = new TestableUnusualFrequency(
        transactionProvider, filterFactory, budgetProvider, 3L);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Entertainment");
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap frequenciesMap = new UserCategoryStatistics.BudgetStatisticsMap();

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
  void shouldHandleNullStatistics() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    UnusualFrequency unusualFrequency = new UnusualFrequency(transactionProvider, filterFactory, budgetProvider);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Travel");
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap frequenciesMap = new UserCategoryStatistics.BudgetStatisticsMap();
    when(statistics.frequencies()).thenReturn(frequenciesMap);

    // Act
    Optional<SpendingInsight> result = unusualFrequency.detect(transaction, statistics);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  void shouldHandleInsufficientData() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    UnusualFrequency unusualFrequency = new UnusualFrequency(transactionProvider, filterFactory, budgetProvider);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Dining");
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap frequenciesMap = new UserCategoryStatistics.BudgetStatisticsMap();

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
