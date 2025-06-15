package com.jongsoft.finance.spending.detector.anomaly;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.domain.insight.SpendingInsight;
import com.jongsoft.finance.domain.transaction.Transaction;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnusualMerchantTest {

  /**
   * This test class validates the `detect` method in UnusualMerchant.
   * The `detect` method identifies transactions with merchants that are not in the list
   * of typical merchants for a budget category.
   */

  @Test
  void shouldDetectUnusualMerchant() {
    // Arrange
    UnusualMerchant unusualMerchant = new UnusualMerchant();

    Transaction transaction = mock(Transaction.class);
    Account merchantAccount = mock(Account.class);
    when(merchantAccount.getName()).thenReturn("New Merchant");
    when(transaction.computeTo()).thenReturn(merchantAccount);
    when(transaction.getBudget()).thenReturn("Groceries");
    when(transaction.getId()).thenReturn(123L);

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
  void shouldNotDetectKnownMerchant() {
    // Arrange
    UnusualMerchant unusualMerchant = new UnusualMerchant();

    Transaction transaction = mock(Transaction.class);
    Account merchantAccount = mock(Account.class);
    when(merchantAccount.getName()).thenReturn("Supermarket");
    when(transaction.computeTo()).thenReturn(merchantAccount);
    when(transaction.getBudget()).thenReturn("Groceries");

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
  void shouldHandleEmptyMerchantList() {
    // Arrange
    UnusualMerchant unusualMerchant = new UnusualMerchant();

    Transaction transaction = mock(Transaction.class);
    Account merchantAccount = mock(Account.class);
    when(merchantAccount.getName()).thenReturn("New Merchant");
    when(transaction.computeTo()).thenReturn(merchantAccount);
    when(transaction.getBudget()).thenReturn("Travel");

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
  void shouldHandleNullMerchantList() {
    // Arrange
    UnusualMerchant unusualMerchant = new UnusualMerchant();

    Transaction transaction = mock(Transaction.class);
    Account merchantAccount = mock(Account.class);
    when(merchantAccount.getName()).thenReturn("New Merchant");
    when(transaction.computeTo()).thenReturn(merchantAccount);
    when(transaction.getBudget()).thenReturn("Entertainment");

    UserCategoryStatistics statistics = mock(UserCategoryStatistics.class);
    Map<String, Set<String>> merchantMap = new HashMap<>();
    when(statistics.typicalMerchants()).thenReturn(merchantMap);

    // Act
    Optional<SpendingInsight> result = unusualMerchant.detect(transaction, statistics);

    // Assert
    assertFalse(result.isPresent());
  }
}
