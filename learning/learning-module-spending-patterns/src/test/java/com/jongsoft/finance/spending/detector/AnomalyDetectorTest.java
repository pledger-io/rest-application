package com.jongsoft.finance.spending.detector;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.detector.anomaly.Anomaly;
import com.jongsoft.finance.spending.detector.anomaly.UserCategoryStatistics;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  private Map<String, ? extends Classifier> forExpense(String expense) {
      return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
  }

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
    doReturn(forExpense("Groceries")).when(transaction).getMetadata();

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
    assertSame(mockInsight, insights.getFirst());
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
    doReturn(Map.of()).when(transaction).getMetadata();

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
    doReturn(forExpense("Groceries")).when(transaction).getMetadata();

    // Act
    List<SpendingInsight> insights = detector.detect(transaction);

    // Assert
    assertTrue(insights.isEmpty());
  }

  @Test
  void shouldUpdateBaselineSuccessfully() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    TestableAnomalyDetector detector = new TestableAnomalyDetector(
        transactionProvider, filterFactory, budgetProvider);

    // Mock transactions and their grouping by budget
    Transaction transaction1 = mock(Transaction.class);
    Transaction transaction2 = mock(Transaction.class);
    doReturn(forExpense("Groceries")).when(transaction1).getMetadata();
    doReturn(forExpense("Groceries")).when(transaction2).getMetadata();
    when(transaction1.computeAmount(any())).thenReturn(100.0);
    when(transaction2.computeAmount(any())).thenReturn(200.0);
    when(filterFactory.transaction()).thenReturn(mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));

    Account merchantAccount = mock(Account.class);
    when(transaction1.computeTo()).thenReturn(merchantAccount);
    when(transaction2.computeTo()).thenReturn(merchantAccount);
    when(merchantAccount.getName()).thenReturn("MerchantA");

    LocalDate now = LocalDate.now();
    when(transaction1.getDate()).thenReturn(now.minusMonths(1));
    when(transaction2.getDate()).thenReturn(now.minusMonths(2));

    when(transactionProvider.lookup(any()))
        .thenReturn(ResultPage.of(transaction1, transaction2));

    // Act
    detector.updateBaseline(YearMonth.now());

    // Assert
    var statistics = detector.getUserCategoryStatistics().get();
    assertNotNull(statistics);
    assertTrue(statistics.amounts().containsKey("Groceries"));
    assertEquals(2, statistics.frequencies().get("Groceries").getN());
    assertEquals(300.0, statistics.amounts().get("Groceries").getSum(), 0.01);
    assertTrue(statistics.typicalMerchants().get("Groceries").contains("MerchantA"));
  }

  @Test
  void shouldClearStatisticsWhenUpdateBaselineCalled() {
    // Arrange
    TransactionProvider transactionProvider = mock(TransactionProvider.class);
    FilterFactory filterFactory = mock(FilterFactory.class);
    BudgetProvider budgetProvider = mock(BudgetProvider.class);

    TestableAnomalyDetector detector = new TestableAnomalyDetector(
        transactionProvider, filterFactory, budgetProvider);

    // Mock transactions for the first update
    Transaction transaction1 = mock(Transaction.class);
    doReturn(forExpense("Entertainment")).when(transaction1).getMetadata();
    when(transaction1.computeAmount(any())).thenReturn(150.0);
    when(transaction1.getDate()).thenReturn(LocalDate.now().minusMonths(1));
    when(filterFactory.transaction()).thenReturn(mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));

    when(transactionProvider.lookup(any())).thenReturn(ResultPage.of(transaction1));

    detector.updateBaseline(YearMonth.now());

    // Mock transactions for the second update
    Transaction transaction2 = mock(Transaction.class);
    doReturn(forExpense("Groceries")).when(transaction2).getMetadata();
    when(transaction2.computeAmount(any())).thenReturn(250.0);
    when(transaction2.getDate()).thenReturn(LocalDate.now().minusMonths(2));

    var transactionsRound2 = List.of(transaction2);
    when(transactionProvider.lookup(any())).thenReturn(ResultPage.of(transaction2));

    // Act
    detector.updateBaseline(YearMonth.now());

    // Assert
    var statistics = detector.getUserCategoryStatistics().get();
    assertNotNull(statistics);
    assertFalse(statistics.amounts().containsKey("Entertainment"));
    assertTrue(statistics.amounts().containsKey("Groceries"));
    assertEquals(250.0, statistics.amounts().get("Groceries").getSum(), 0.01);
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
        if (!userStatistics.amounts().containsKey(((EntityRef.NamedEntity)transaction.getMetadata().get("EXPENSE")).name())) {
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
