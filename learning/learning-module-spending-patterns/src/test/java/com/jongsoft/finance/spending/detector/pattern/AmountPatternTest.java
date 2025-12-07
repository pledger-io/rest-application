package com.jongsoft.finance.spending.detector.pattern;

import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Category;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AmountPatternTest {

  /**
   * This test class validates the `detect` method in AmountPattern.
   * The `detect` method identifies increasing or decreasing trends in transaction amounts.
   */

  private Map<String, ? extends Classifier> forCategory(String category) {
      return Map.of("CATEGORY", Category.builder().label(category).build());
  }

  @Test
  void shouldDetectIncreasingTrend() {
    // Arrange
    AmountPattern amountPattern = new AmountPattern();

    Transaction transaction = mock(Transaction.class);
    doReturn(forCategory("Groceries")).when(transaction).getMetadata();
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(150.0); // Current amount

    // Create a list of embedding matches with increasing amounts
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

    // Add matches with dates and amounts in chronological order (increasing trend)
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 1), 80.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 15), 85.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 1), 90.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 15), 95.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 1), 100.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 15), 110.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 1), 120.0));

    // Act
    Optional<SpendingPattern> result = amountPattern.detect(transaction, matches);

    // Assert
    assertTrue(result.isPresent());
    SpendingPattern pattern = result.get();
    assertEquals(PatternType.INCREASING_TREND, pattern.getType());
    assertEquals("Groceries", pattern.getCategory());
    assertEquals(LocalDate.of(2025, 5, 1), pattern.getDetectedDate());
    assertTrue(pattern.getMetadata().containsKey("typical_amount"));
    assertTrue(pattern.getMetadata().containsKey("current_amount"));
    assertTrue(pattern.getMetadata().containsKey("deviation_percent"));
  }

  @Test
  void shouldDetectDecreasingTrend() {
    // Arrange
    AmountPattern amountPattern = new AmountPattern();

    Transaction transaction = mock(Transaction.class);
    doReturn(forCategory("Utilities")).when(transaction).getMetadata();
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(80.0); // Current amount

    // Create a list of embedding matches with decreasing amounts
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

    // Add matches with dates and amounts in chronological order (decreasing trend)
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 1), 150.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 15), 140.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 1), 130.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 15), 120.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 1), 110.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 15), 100.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 1), 90.0));

    // Act
    Optional<SpendingPattern> result = amountPattern.detect(transaction, matches);

    // Assert
    assertTrue(result.isPresent());
    SpendingPattern pattern = result.get();
    assertEquals(PatternType.DECREASING_TREND, pattern.getType());
    assertEquals("Utilities", pattern.getCategory());
    assertEquals(LocalDate.of(2025, 5, 1), pattern.getDetectedDate());
    assertTrue(pattern.getMetadata().containsKey("typical_amount"));
    assertTrue(pattern.getMetadata().containsKey("current_amount"));
    assertTrue(pattern.getMetadata().containsKey("deviation_percent"));
  }

  @Test
  void shouldNotDetectTrendWhenAmountsAreStable() {
    // Arrange
    AmountPattern amountPattern = new AmountPattern();

    Transaction transaction = mock(Transaction.class);
    doReturn(forCategory("Entertainment")).when(transaction).getMetadata();
    when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(100.0); // Current amount

    // Create a list of embedding matches with stable amounts
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

    // Add matches with dates and stable amounts
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 1), 95.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 15), 105.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 1), 98.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 15), 102.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 1), 97.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 15), 103.0));
    matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 1), 100.0));

    // Act
    Optional<SpendingPattern> result = amountPattern.detect(transaction, matches);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  void shouldHandleEmptyMatches() {
    // Arrange
    AmountPattern amountPattern = new AmountPattern();

    Transaction transaction = mock(Transaction.class);
    doReturn(forCategory("Travel")).when(transaction).getMetadata();

    // Create an empty list of embedding matches
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

    // Act
    Optional<SpendingPattern> result = amountPattern.detect(transaction, matches);

    // Assert
    assertFalse(result.isPresent());
  }

  /**
   * Helper method to create a mocked EmbeddingMatch with the given date and amount
   */
  @SuppressWarnings("unchecked")
  private EmbeddingMatch<TextSegment> createEmbeddingMatch(LocalDate date, double amount) {
    TextSegment segment = mock(TextSegment.class);
    dev.langchain4j.data.document.Metadata metadata = mock(dev.langchain4j.data.document.Metadata.class);

    when(segment.metadata()).thenReturn(metadata);
    when(metadata.getString("date")).thenReturn(date.toString());
    when(metadata.getDouble("amount")).thenReturn(amount);

    EmbeddingMatch<TextSegment> match = mock(EmbeddingMatch.class);
    when(match.embedded()).thenReturn(segment);
    when(match.score()).thenReturn(0.95);

    return match;
  }
}
