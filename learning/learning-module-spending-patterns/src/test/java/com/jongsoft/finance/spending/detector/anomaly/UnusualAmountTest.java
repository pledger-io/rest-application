package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnusualAmountTest {

  /**
   * This test class validates the `detect` method in UnusualAmount.
   * The `detect` method identifies transactions with amounts that are unusually high or low
   * compared to the typical amounts for a budget category.
   */

  @Test
  void shouldDetectUnusuallyHighAmount() {
    // Arrange
    UnusualAmount unusualAmount = new UnusualAmount();

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Groceries");
    when(transaction.getId()).thenReturn(123L);
    when(transaction.getDate()).thenReturn(java.time.LocalDate.now());
    when(transaction.computeTo()).thenReturn(mock(com.jongsoft.finance.domain.account.Account.class));
    when(transaction.computeAmount(any())).thenReturn(500.0); // Unusually high amount

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap amountsMap = new UserCategoryStatistics.BudgetStatisticsMap();

    DescriptiveStatistics groceriesStats = new DescriptiveStatistics();
    // Add typical amounts for groceries
    groceriesStats.addValue(100.0);
    groceriesStats.addValue(120.0);
    groceriesStats.addValue(90.0);
    groceriesStats.addValue(110.0);
    groceriesStats.addValue(105.0);
    amountsMap.put("Groceries", groceriesStats);

    when(statistics.amounts()).thenReturn(amountsMap);

    // Act
    Optional<SpendingInsight> result = unusualAmount.detect(transaction, statistics);

    // Assert
    assertTrue(result.isPresent());
    SpendingInsight insight = result.get();
    assertEquals("Groceries", insight.getCategory());
    assertEquals("computed.insight.amount.high", insight.getMessage());
    assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal()); // Should be at least WARNING
    assertTrue(insight.getMetadata().containsKey("amount"));
    assertTrue(insight.getMetadata().containsKey("z_score"));
    assertTrue(insight.getMetadata().containsKey("mean"));
    assertTrue(insight.getMetadata().containsKey("std_dev"));
  }

  @Test
  void shouldDetectUnusuallyLowAmount() {
    // Arrange
    UnusualAmount unusualAmount = new UnusualAmount();

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Utilities");
    when(transaction.getId()).thenReturn(456L);
    when(transaction.getDate()).thenReturn(java.time.LocalDate.now());
    when(transaction.computeTo()).thenReturn(mock(com.jongsoft.finance.domain.account.Account.class));
    when(transaction.computeAmount(any())).thenReturn(10.0); // Unusually low amount

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap amountsMap = new UserCategoryStatistics.BudgetStatisticsMap();

    DescriptiveStatistics utilitiesStats = new DescriptiveStatistics();
    // Add typical amounts for utilities
    utilitiesStats.addValue(100.0);
    utilitiesStats.addValue(120.0);
    utilitiesStats.addValue(90.0);
    utilitiesStats.addValue(110.0);
    utilitiesStats.addValue(105.0);
    amountsMap.put("Utilities", utilitiesStats);

    when(statistics.amounts()).thenReturn(amountsMap);

    // Act
    Optional<SpendingInsight> result = unusualAmount.detect(transaction, statistics);

    // Assert
    assertTrue(result.isPresent());
    SpendingInsight insight = result.get();
    assertEquals("Utilities", insight.getCategory());
    assertEquals("computed.insight.amount.low", insight.getMessage());
    assertTrue(insight.getSeverity().ordinal() >= Severity.WARNING.ordinal()); // Should be at least WARNING
  }

  @Test
  void shouldNotDetectUnusualAmountWhenWithinNormalRange() {
    // Arrange
    UnusualAmount unusualAmount = new UnusualAmount();

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Entertainment");
    when(transaction.computeTo()).thenReturn(mock(com.jongsoft.finance.domain.account.Account.class));
    when(transaction.computeAmount(any())).thenReturn(105.0); // Normal amount

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap amountsMap = new UserCategoryStatistics.BudgetStatisticsMap();

    DescriptiveStatistics entertainmentStats = new DescriptiveStatistics();
    // Add typical amounts for entertainment
    entertainmentStats.addValue(100.0);
    entertainmentStats.addValue(120.0);
    entertainmentStats.addValue(90.0);
    entertainmentStats.addValue(110.0);
    entertainmentStats.addValue(105.0);
    amountsMap.put("Entertainment", entertainmentStats);

    when(statistics.amounts()).thenReturn(amountsMap);

    // Act
    Optional<SpendingInsight> result = unusualAmount.detect(transaction, statistics);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  void shouldHandleNullStatistics() {
    // Arrange
    UnusualAmount unusualAmount = new UnusualAmount();

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Travel");

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap amountsMap = new UserCategoryStatistics.BudgetStatisticsMap();
    when(statistics.amounts()).thenReturn(amountsMap);

    // Act
    Optional<SpendingInsight> result = unusualAmount.detect(transaction, statistics);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  void shouldHandleInsufficientData() {
    // Arrange
    UnusualAmount unusualAmount = new UnusualAmount();

    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Dining");
    when(transaction.computeTo()).thenReturn(mock(com.jongsoft.finance.domain.account.Account.class));
    when(transaction.computeAmount(any())).thenReturn(50.0);

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    UserCategoryStatistics.BudgetStatisticsMap amountsMap = new UserCategoryStatistics.BudgetStatisticsMap();

    DescriptiveStatistics diningStats = new DescriptiveStatistics();
    // Add only a few data points (less than 5)
    diningStats.addValue(40.0);
    diningStats.addValue(45.0);
    amountsMap.put("Dining", diningStats);

    when(statistics.amounts()).thenReturn(amountsMap);

    // Act
    Optional<SpendingInsight> result = unusualAmount.detect(transaction, statistics);

    // Assert
    assertFalse(result.isPresent());
  }
}
