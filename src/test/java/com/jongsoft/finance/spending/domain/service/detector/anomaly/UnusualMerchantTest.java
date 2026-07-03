package com.jongsoft.finance.spending.domain.service.detector.anomaly;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.domain.model.Account;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingInsight;
import com.jongsoft.finance.spending.types.Severity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@DisplayName("Unit - Unusual Merchant")
class UnusualMerchantTest {

    /**
     * This test class validates the `detect` method in UnusualMerchant.
     * The `detect` method identifies transactions with merchants that are not in the list
     * of typical merchants for a budget category.
     */
    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @Test
    @DisplayName("Should detect unusual merchant")
    void shouldDetectUnusualMerchant() {
        // Arrange
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("New Merchant");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        when(transaction.computeAmount(merchantAccount)).thenReturn(150.0);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getId()).thenReturn(123L);

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        Set<String> typicalMerchants = new HashSet<>();
        typicalMerchants.add("Regular Grocery Store");
        typicalMerchants.add("Supermarket");
        typicalMerchants.add("Local Market");

        Map<String, Set<String>> merchantMap = new HashMap<>();
        merchantMap.put("Groceries", typicalMerchants);
        when(statistics.typicalMerchants()).thenReturn(merchantMap);
        when(statistics.amounts()).thenReturn(new UserCategoryStatistics.BudgetStatisticsMap());

        // Act
        Optional<SpendingInsight> result = unusualMerchant.detect(transaction, statistics);

        // Assert
        assertTrue(result.isPresent());
        SpendingInsight insight = result.get();
        assertEquals("Groceries", insight.getCategory());
        assertEquals("computed.insight.merchant.unusual", insight.getMessage());
        assertEquals(Severity.INFO, insight.getSeverity());
        assertTrue(insight.getMetadata().containsKey("merchant"));
        assertTrue(insight.getMetadata().containsKey("known_merchants_count"));
        assertEquals("New Merchant", insight.getMetadata().get("merchant"));
        assertEquals(3, insight.getMetadata().get("known_merchants_count"));
    }

    @Test
    @DisplayName("Do not detect known merchant")
    void shouldNotDetectKnownMerchant() {
        // Arrange
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("Supermarket");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        Set<String> typicalMerchants = new HashSet<>();
        typicalMerchants.add("Regular Grocery Store");
        typicalMerchants.add("Supermarket");
        typicalMerchants.add("Local Market");

        Map<String, Set<String>> merchantMap = new HashMap<>();
        merchantMap.put("Groceries", typicalMerchants);
        when(statistics.typicalMerchants()).thenReturn(merchantMap);

        // Act
        Optional<SpendingInsight> result = unusualMerchant.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Do not detect unusual merchant when amount is below category mean")
    void shouldNotDetectUnusualMerchantWhenAmountIsNotSignificant() {
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("New Merchant");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        when(transaction.computeAmount(merchantAccount)).thenReturn(5.0);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = new UserCategoryStatistics(12);
        statistics.typicalMerchants().put("Groceries", java.util.Set.of("Supermarket"));

        org.apache.commons.math3.stat.descriptive.DescriptiveStatistics amounts =
                new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics();
        amounts.addValue(100.0);
        amounts.addValue(120.0);
        amounts.addValue(90.0);
        amounts.addValue(110.0);
        amounts.addValue(105.0);
        statistics.amounts().put("Groceries", amounts);

        assertFalse(unusualMerchant.detect(transaction, statistics).isPresent());
    }

    @Test
    @DisplayName("Do not detect unusual merchant when computeTo is null")
    void shouldNotDetectUnusualMerchantWhenMerchantAccountIsNull() {
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        when(transaction.computeTo()).thenReturn(null);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = new UserCategoryStatistics(12);
        statistics.typicalMerchants().put("Groceries", java.util.Set.of("Supermarket"));

        assertFalse(unusualMerchant.detect(transaction, statistics).isPresent());
    }

    @Test
    @DisplayName(
            "Do not detect unusual merchant when baseline amount data is insufficient and amount is zero")
    void shouldNotDetectWhenInsufficientBaselineAndZeroAmount() {
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("New Merchant");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        when(transaction.computeAmount(merchantAccount)).thenReturn(0.0);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = new UserCategoryStatistics(12);
        statistics.typicalMerchants().put("Groceries", java.util.Set.of("Supermarket"));
        statistics
                .amounts()
                .put(
                        "Groceries",
                        new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics());

        assertFalse(unusualMerchant.detect(transaction, statistics).isPresent());
    }

    @Test
    @DisplayName("Handle empty merchant list gracefully")
    void shouldHandleEmptyMerchantList() {
        // Arrange
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("New Merchant");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        doReturn(forExpense("Travel")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        Map<String, Set<String>> merchantMap = new HashMap<>();
        merchantMap.put("Travel", new HashSet<>());
        when(statistics.typicalMerchants()).thenReturn(merchantMap);

        // Act
        Optional<SpendingInsight> result = unusualMerchant.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle null merchant list gracefully")
    void shouldHandleNullMerchantList() {
        // Arrange
        UnusualMerchant unusualMerchant = new UnusualMerchant();

        Transaction transaction = mock(Transaction.class);
        Account merchantAccount = mock(Account.class);
        when(merchantAccount.getName()).thenReturn("New Merchant");
        when(transaction.computeTo()).thenReturn(merchantAccount);
        doReturn(forExpense("Entertainment")).when(transaction).getMetadata();

        UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
        Map<String, Set<String>> merchantMap = new HashMap<>();
        when(statistics.typicalMerchants()).thenReturn(merchantMap);

        // Act
        Optional<SpendingInsight> result = unusualMerchant.detect(transaction, statistics);

        // Assert
        assertFalse(result.isPresent());
    }
}
