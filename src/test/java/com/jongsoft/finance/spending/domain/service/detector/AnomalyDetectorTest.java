package com.jongsoft.finance.spending.domain.service.detector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.domain.service.detector.anomaly.Anomaly;
import com.jongsoft.finance.spending.domain.service.detector.anomaly.UserCategoryStatistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Anomaly Detector")
class AnomalyDetectorTest {

    private static final SpendingAnalysisConfiguration SETTINGS =
            new SpendingAnalysisConfiguration();

    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @Test
    void shouldBeReadyForAnalysis() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        AnomalyDetector anomalyDetector =
                new AnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        assertTrue(anomalyDetector.readyForAnalysis());
    }

    @Test
    void shouldDetectAnomaliesWhenBudgetHasStatistics() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        TestableAnomalyDetector detector =
                new TestableAnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        Anomaly mockAnomaly = mock(Anomaly.class);
        SpendingInsight mockInsight = mock(SpendingInsight.class);
        when(mockAnomaly.detect(any(), any())).thenReturn(Optional.of(mockInsight));

        detector.setAnomalies(List.of(mockAnomaly));
        detector.setupStatisticsForBudget("Groceries");

        List<SpendingInsight> insights = detector.detect(transaction);

        assertEquals(1, insights.size());
        assertSame(mockInsight, insights.getFirst());
        verify(mockAnomaly).detect(eq(transaction), any());
    }

    @Test
    void shouldReturnEmptyListWhenNoBudget() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        AnomalyDetector anomalyDetector =
                new AnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        Transaction transaction = mock(Transaction.class);
        doReturn(Map.of()).when(transaction).getMetadata();

        assertTrue(anomalyDetector.detect(transaction).isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoStatisticsForBudget() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        TestableAnomalyDetector detector =
                new TestableAnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        assertTrue(detector.detect(transaction).isEmpty());
    }

    @Test
    void shouldUpdateBaselineSuccessfully() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        TestableAnomalyDetector detector =
                new TestableAnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        Transaction transaction1 = mock(Transaction.class);
        Transaction transaction2 = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction1).getMetadata();
        doReturn(forExpense("Groceries")).when(transaction2).getMetadata();
        when(transaction1.computeAmount(any())).thenReturn(100.0);
        when(transaction2.computeAmount(any())).thenReturn(200.0);
        when(filterFactory.create())
                .thenReturn(
                        mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));

        Account merchantAccount = mock(Account.class);
        when(transaction1.computeTo()).thenReturn(merchantAccount);
        when(transaction2.computeTo()).thenReturn(merchantAccount);
        when(merchantAccount.getName()).thenReturn("MerchantA");

        YearMonth analyzedMonth = YearMonth.of(2025, 6);
        when(transaction1.getDate()).thenReturn(analyzedMonth.minusMonths(1).atDay(15));
        when(transaction2.getDate()).thenReturn(analyzedMonth.minusMonths(2).atDay(15));

        when(transactionProvider.lookup(any()))
                .thenReturn(ResultPage.of(transaction1, transaction2));

        detector.updateBaseline(analyzedMonth);

        var statistics = detector.getUserCategoryStatistics().get();
        assertNotNull(statistics);
        assertTrue(statistics.amounts().containsKey("Groceries"));
        assertEquals(12, statistics.frequencies().get("Groceries").getN());
        assertEquals(300.0, statistics.amounts().get("Groceries").getSum(), 0.01);
        assertEquals(12, statistics.monthlyTotals().get("Groceries").getN());
        assertTrue(statistics.typicalMerchants().get("Groceries").contains("MerchantA"));
        assertEquals(12, statistics.baselineMonths());
    }

    @Test
    void shouldClearStatisticsWhenUpdateBaselineCalled() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        TestableAnomalyDetector detector =
                new TestableAnomalyDetector(transactionProvider, filterFactory, SETTINGS);

        Transaction transaction1 = mock(Transaction.class);
        doReturn(forExpense("Entertainment")).when(transaction1).getMetadata();
        when(transaction1.computeAmount(any())).thenReturn(150.0);
        when(transaction1.getDate()).thenReturn(LocalDate.now().minusMonths(1));
        when(filterFactory.create())
                .thenReturn(
                        mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));

        when(transactionProvider.lookup(any())).thenReturn(ResultPage.of(transaction1));
        detector.updateBaseline(YearMonth.now());

        Transaction transaction2 = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction2).getMetadata();
        when(transaction2.computeAmount(any())).thenReturn(250.0);
        when(transaction2.getDate()).thenReturn(LocalDate.now().minusMonths(2));
        when(transactionProvider.lookup(any())).thenReturn(ResultPage.of(transaction2));

        detector.updateBaseline(YearMonth.now());

        var statistics = detector.getUserCategoryStatistics().get();
        assertNotNull(statistics);
        assertFalse(statistics.amounts().containsKey("Entertainment"));
        assertTrue(statistics.amounts().containsKey("Groceries"));
        assertEquals(250.0, statistics.amounts().get("Groceries").getSum(), 0.01);
    }

    @Test
    void shouldDetectMonthLevelAnomalyForCategoryAbsentInAnalyzedMonth() {
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);

        SpendingAnalysisConfiguration config = new SpendingAnalysisConfiguration();
        config.setBaselineMonths(3);

        AnomalyDetector detector = new AnomalyDetector(transactionProvider, filterFactory, config);

        YearMonth analyzedMonth = YearMonth.of(2025, 6);
        Account merchant = mock(Account.class);
        when(merchant.getName()).thenReturn("Store");

        List<Transaction> groceriesBaseline = new ArrayList<>();
        groceriesBaseline.addAll(repeatBaselineTransactions(
                "Groceries", analyzedMonth.minusMonths(3), merchant, 5, 100.0));
        groceriesBaseline.addAll(repeatBaselineTransactions(
                "Groceries", analyzedMonth.minusMonths(2), merchant, 4, 100.0));
        groceriesBaseline.addAll(repeatBaselineTransactions(
                "Groceries", analyzedMonth.minusMonths(1), merchant, 6, 100.0));

        Transaction utilitiesJun =
                analyzedMonthTransaction("Utilities", analyzedMonth, 50.0, merchant);

        when(filterFactory.create())
                .thenReturn(
                        mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));
        when(transactionProvider.lookup(any()))
                .thenReturn(ResultPage.of(groceriesBaseline.toArray(Transaction[]::new)));

        detector.updateBaseline(analyzedMonth);

        List<SpendingInsight> insights =
                detector.detectForMonth(analyzedMonth, List.of(utilitiesJun));

        assertTrue(insights.stream()
                .anyMatch(insight -> "Groceries".equals(insight.getCategory())
                        && insight.getTransactionId() == null));
        assertTrue(insights.stream()
                .anyMatch(insight -> "Groceries".equals(insight.getCategory())
                        && "DOWN".equals(insight.getMetadata().get("direction"))));
    }

    private List<Transaction> repeatBaselineTransactions(
            String expense, YearMonth month, Account merchant, int count, double amount) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(baselineTransaction(expense, month, amount, merchant));
        }
        return transactions;
    }

    private Transaction baselineTransaction(
            String expense, YearMonth month, double amount, Account merchant) {
        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense(expense)).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(month.atDay(10));
        when(transaction.computeTo()).thenReturn(merchant);
        when(transaction.computeAmount(merchant)).thenReturn(amount);
        return transaction;
    }

    private Transaction analyzedMonthTransaction(
            String expense, YearMonth month, double amount, Account merchant) {
        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense(expense)).when(transaction).getMetadata();
        when(transaction.getId()).thenReturn(99L);
        when(transaction.getDate()).thenReturn(month.atDay(15));
        when(transaction.computeTo()).thenReturn(merchant);
        when(transaction.computeAmount(merchant)).thenReturn(amount);
        return transaction;
    }

    private static class TestableAnomalyDetector extends AnomalyDetector {
        private List<Anomaly> testAnomalies = new ArrayList<>();

        TestableAnomalyDetector(
                TransactionProvider transactionProvider,
                FilterProvider<TransactionProvider.FilterCommand> filterFactory,
                SpendingAnalysisConfiguration settings) {
            super(transactionProvider, filterFactory, settings);
        }

        void setAnomalies(List<Anomaly> anomalies) {
            this.testAnomalies = anomalies;
        }

        void setupStatisticsForBudget(String budget) {
            UserCategoryStatistics statistics = new UserCategoryStatistics(12);
            statistics
                    .amounts()
                    .put(
                            budget,
                            new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics());
            getUserCategoryStatistics().set(statistics);
        }

        @Override
        public List<SpendingInsight> detect(Transaction transaction) {
            if (!testAnomalies.isEmpty()) {
                var userStatistics = getUserCategoryStatistics().get();
                if (!userStatistics
                        .amounts()
                        .containsKey(((EntityRef.NamedEntity)
                                        transaction.getMetadata().get("EXPENSE"))
                                .name())) {
                    return List.of();
                }

                return testAnomalies.stream()
                        .map(anomaly -> anomaly.detect(transaction, userStatistics))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
            }

            return super.detect(transaction);
        }

        ThreadLocal<UserCategoryStatistics> getUserCategoryStatistics() {
            try {
                java.lang.reflect.Field field =
                        AnomalyDetector.class.getDeclaredField("userCategoryStatistics");
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
