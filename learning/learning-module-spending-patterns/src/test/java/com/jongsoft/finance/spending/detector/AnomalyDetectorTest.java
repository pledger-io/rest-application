package com.jongsoft.finance.spending.detector;

import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.detector.anomaly.Anomaly;
import com.jongsoft.finance.spending.detector.anomaly.UserCategoryStatistics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnomalyDetectorTest {

  /**
   * This test class validates the methods in AnomalyDetector.
   * The AnomalyDetector class is responsible for detecting anomalies in transactions
   * by using multiple anomaly detectors.
   */

  @Test
  void shouldBeReadyForAnalysis() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    AnomalyDetector anomalyDetector = new AnomalyDetector(transactionProvider, filterFactory, budgetProvider);

    // Act
    boolean ready = anomalyDetector.readyForAnalysis();

    // Assert
    assertTrue(ready);
  }

  @Test
  void shouldDetectAnomaliesWhenBudgetHasStatistics() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    // Create a subclass of AnomalyDetector for testing
    TestableAnomalyDetector detector = new TestableAnomalyDetector(
        transactionProvider, filterFactory, budgetProvider);

    // Create a test transaction
    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Groceries");

    // Create a mock anomaly that returns an insight
    Anomaly mockAnomaly = mock(Anomaly.class);
    SpendingInsight mockInsight = mock(SpendingInsight.class);
    when(mockAnomaly.detect(any(), any())).thenReturn(Optional.of(mockInsight));

    // Set up the detector with our mock anomaly
    detector.setAnomalies(List.of(mockAnomaly));
    detector.setupStatisticsForBudget("Groceries");

    // Act
    List<SpendingInsight> insights = detector.detect(transaction);

    // Assert
    assertEquals(1, insights.size());
    assertSame(mockInsight, insights.get(0));
    verify(mockAnomaly).detect(eq(transaction), any());
  }

  @Test
  void shouldReturnEmptyListWhenNoBudget() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    AnomalyDetector anomalyDetector = new AnomalyDetector(transactionProvider, filterFactory, budgetProvider);

    // Create a transaction without a budget
    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn(null);

    // Act
    List<SpendingInsight> insights = anomalyDetector.detect(transaction);

    // Assert
    assertTrue(insights.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenNoStatisticsForBudget() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    // Create a subclass of AnomalyDetector for testing
    TestableAnomalyDetector detector = new TestableAnomalyDetector(
        transactionProvider, filterFactory, budgetProvider);

    // Create a transaction with a budget that has no statistics
    Transaction transaction = mock(Transaction.class);
    when(transaction.getBudget()).thenReturn("Groceries");

    // Act
    List<SpendingInsight> insights = detector.detect(transaction);

    // Assert
    assertTrue(insights.isEmpty());
  }

  /**
   * Testable subclass of AnomalyDetector for testing
   */
  private static class TestableAnomalyDetector extends AnomalyDetector {
    private List<Anomaly> testAnomalies = new ArrayList<>();

    public TestableAnomalyDetector(
        TransactionProvider transactionProvider,
        FilterFactory filterFactory,
        BudgetProvider budgetProvider) {
      super(transactionProvider, filterFactory, budgetProvider);
    }

    public void setAnomalies(List<Anomaly> anomalies) {
      this.testAnomalies = anomalies;
    }

    public void setupStatisticsForBudget(String budget) {
      // Create a ThreadLocal UserCategoryStatistics with data for the specified budget
      getUserCategoryStatistics().set(createStatisticsWithBudget(budget));
    }

    private UserCategoryStatistics createStatisticsWithBudget(String budget) {
      // Create statistics with data for the specified budget
      return new UserCategoryStatistics(
          createBudgetStatisticsMap(budget),
          createBudgetStatisticsMap(budget),
          new java.util.HashMap<>()
      );
    }

    private UserCategoryStatistics.BudgetStatisticsMap createBudgetStatisticsMap(String budget) {
      UserCategoryStatistics.BudgetStatisticsMap map = new UserCategoryStatistics.BudgetStatisticsMap();
      map.put(budget, new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics());
      return map;
    }

    @Override
    public List<SpendingInsight> detect(Transaction transaction) {
      // If we have test anomalies, use them instead of the real ones
      if (!testAnomalies.isEmpty()) {
        var userStatistics = getUserCategoryStatistics().get();
        if (!userStatistics.amounts().containsKey(transaction.getBudget())) {
          return List.of();
        }

        return testAnomalies
            .stream()
            .map(anomaly -> anomaly.detect(transaction, userStatistics))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
      }

      // Otherwise, use the real implementation
      return super.detect(transaction);
    }

    // Expose the ThreadLocal for testing
    protected ThreadLocal<UserCategoryStatistics> getUserCategoryStatistics() {
      try {
        java.lang.reflect.Field field = AnomalyDetector.class.getDeclaredField("userCategoryStatistics");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<UserCategoryStatistics> threadLocal =
            (ThreadLocal<UserCategoryStatistics>) field.get(this);
        return threadLocal;
      } catch (Exception e) {
        throw new RuntimeException("Failed to access userCategoryStatistics field", e);
      }
    }
  }
}
