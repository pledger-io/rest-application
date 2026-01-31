package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import io.micronaut.context.event.ApplicationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Spending Spike")
class SpendingSpikeTest {

    /**
     * This test class validates the `detect` method in SpendingSpike. The `detect` method
     * identifies potential anomalies in spending patterns by comparing the current month's total
     * spending against the average of the last few months' spending.
     */
    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @BeforeEach
    void setUp() {
        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Detect spending spike")
    void shouldDetectSpendingSpikeWhenCurrentMonthSpendingExceedsThreshold() {
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);
        SpendingSpike spendingSpike =
                new SpendingSpike(filterFactory, transactionProvider, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 1));
        doReturn(forExpense("Food")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);

        // Mocking monthly spending data
        Map<String, Double> monthlyData = Map.of(
                "2025-2", 200.0,
                "2025-3", 250.0,
                "2025-4", 300.0,
                "2025-5", 500.0);
        SpendingSpike spySpendingSpike = spy(spendingSpike);
        doReturn(monthlyData).when(spySpendingSpike).computeSpendingPerMonth(transaction, 4);

        Optional<SpendingInsight> result = spySpendingSpike.detect(transaction, statistics);

        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Food", insight.getCategory());
        assertEquals("computed.insight.spending.spike", insight.getMessage());
        assertEquals(Severity.WARNING, insight.getSeverity());
        assertTrue(insight.getMetadata().containsKey("current_month_total"));
        assertTrue(insight.getMetadata().containsKey("avg_monthly_spending"));
    }

    @Test
    @DisplayName("Do not detect spending spike")
    void shouldNotDetectSpendingSpikeWhenCurrentMonthSpendingWithinThreshold() {
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);
        SpendingSpike spendingSpike =
                new SpendingSpike(filterFactory, transactionProvider, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 1));
        doReturn(forExpense("Utilities")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);

        // Mocking monthly spending data
        Map<String, Double> monthlyData = Map.of(
                "2025-2", 100.0,
                "2025-3", 120.0,
                "2025-4", 150.0,
                "2025-5", 160.0);
        SpendingSpike spySpendingSpike = spy(spendingSpike);
        doReturn(monthlyData).when(spySpendingSpike).computeSpendingPerMonth(transaction, 4);

        Optional<SpendingInsight> result = spySpendingSpike.detect(transaction, statistics);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Handle insufficient monthly spending data gracefully")
    void shouldHandleEmptyMonthlySpendingDataGracefully() {
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);
        SpendingSpike spendingSpike =
                new SpendingSpike(filterFactory, transactionProvider, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 1));
        doReturn(forExpense("Travel")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);

        // Mocking monthly spending data
        Map<String, Double> monthlyData = new HashMap<>();
        SpendingSpike spySpendingSpike = spy(spendingSpike);
        doReturn(monthlyData).when(spySpendingSpike).computeSpendingPerMonth(transaction, 4);

        Optional<SpendingInsight> result = spySpendingSpike.detect(transaction, statistics);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Detect spending spike with edge case values")
    void shouldDetectSpendingSpikeWithEdgeCaseValues() {
        FilterProvider<TransactionProvider.FilterCommand> filterFactory =
                mock(FilterProvider.class);
        TransactionProvider transactionProvider = mock(TransactionProvider.class);
        BudgetProvider budgetProvider = mock(BudgetProvider.class);
        SpendingSpike spendingSpike =
                new SpendingSpike(filterFactory, transactionProvider, budgetProvider);

        Transaction transaction = mock(Transaction.class);
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 1));
        doReturn(forExpense("Entertainment")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);

        // Mocking monthly spending data
        Map<String, Double> monthlyData = Map.of(
                "2025-2", 0.0,
                "2025-3", 0.0,
                "2025-4", 0.0,
                "2025-5", 200.0);
        SpendingSpike spySpendingSpike = spy(spendingSpike);
        doReturn(monthlyData).when(spySpendingSpike).computeSpendingPerMonth(transaction, 4);

        Optional<SpendingInsight> result = spySpendingSpike.detect(transaction, statistics);

        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Entertainment", insight.getCategory());
        assertEquals("computed.insight.spending.spike", insight.getMessage());
        assertEquals(Severity.ALERT, insight.getSeverity());
        assertTrue(insight.getMetadata().containsKey("current_month_total"));
        assertTrue(insight.getMetadata().containsKey("avg_monthly_spending"));
    }
}
