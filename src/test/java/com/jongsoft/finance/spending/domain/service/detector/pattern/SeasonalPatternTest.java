package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Seasonal Patterns")
class SeasonalPatternTest {

    /**
     * This test class validates the `detect` method in SeasonalPattern.
     * The `detect` method identifies seasonal patterns in transactions.
     */
    private Map<String, ? extends Classifier> forCategory(String category) {
        return Map.of("CATEGORY", Category.create(category, ""));
    }

    @Test
    @DisplayName("Should detect seasonal pattern")
    void shouldDetectSummerPattern() {
        // Arrange
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Travel")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 6, 15)); // Summer

        // Create a list of embedding matches with summer concentration
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add a few transactions in non-summer months
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 1, 15), 100.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 3, 10), 150.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 10, 5), 120.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 2, 20), 130.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 4, 12), 110.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 11, 8), 140.0));

        // Add many transactions in summer months (June, July, August)
        for (int year = 2023; year <= 2024; year++) {
            for (int month = 6; month <= 8; month++) {
                for (int day = 1; day <= 28; day += 7) {
                    matches.add(createEmbeddingMatch(LocalDate.of(year, month, day), 200.0));
                }
            }
        }

        // Act
        Optional<SpendingPattern> result = seasonalPattern.detect(transaction, matches);

        // Assert
        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.SEASONAL, pattern.getType());
        assertEquals("Travel", pattern.getCategory());
        assertEquals(LocalDate.of(2025, 6, 1), pattern.getDetectedDate());
        assertTrue(pattern.getMetadata().containsKey("season"));
        assertEquals("Summer", pattern.getMetadata().get("season"));
    }

    @Test
    @DisplayName("Should detect seasonal pattern")
    void shouldDetectWinterPattern() {
        // Arrange
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Skiing")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 1, 15)); // Winter

        // Create a list of embedding matches with winter concentration
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add a few transactions in non-winter months
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 4, 15), 100.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 7, 10), 150.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2023, 10, 5), 120.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 5, 20), 130.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 8, 12), 110.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2024, 9, 8), 140.0));

        // Add many transactions in winter months (December, January, February)
        for (int year = 2023; year <= 2024; year++) {
            // December of previous year
            for (int day = 1; day <= 28; day += 7) {
                matches.add(createEmbeddingMatch(LocalDate.of(year - 1, 12, day), 200.0));
            }
            // January and February of current year
            for (int month = 1; month <= 2; month++) {
                for (int day = 1; day <= 28; day += 7) {
                    matches.add(createEmbeddingMatch(LocalDate.of(year, month, day), 200.0));
                }
            }
        }

        // Act
        Optional<SpendingPattern> result = seasonalPattern.detect(transaction, matches);

        // Assert
        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.SEASONAL, pattern.getType());
        assertEquals("Skiing", pattern.getCategory());
        assertEquals(LocalDate.of(2025, 1, 1), pattern.getDetectedDate());
        assertTrue(pattern.getMetadata().containsKey("season"));
        assertEquals("Winter", pattern.getMetadata().get("season"));
    }

    @Test
    @DisplayName("Should not detect seasonal pattern when distribution is even")
    void shouldNotDetectSeasonalPatternWhenDistributionIsEven() {
        // Arrange
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Groceries")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list of embedding matches with even distribution across months
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add transactions evenly distributed across all months
        for (int year = 2023; year <= 2024; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int day = 1; day <= 28; day += 7) {
                    matches.add(createEmbeddingMatch(LocalDate.of(year, month, day), 100.0));
                }
            }
        }

        // Act
        Optional<SpendingPattern> result = seasonalPattern.detect(transaction, matches);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle null matches gracefully")
    void shouldHandleEmptyMatches() {
        // Arrange
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Entertainment")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create an empty list of embedding matches
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Act
        Optional<SpendingPattern> result = seasonalPattern.detect(transaction, matches);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle insufficient matches gracefully")
    void shouldHandleInsufficientMatches() {
        // Arrange
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Dining")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list with only a few matches (not enough to establish a seasonal pattern)
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 1), 75.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 1), 80.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 6, 1), 85.0));

        // Act
        Optional<SpendingPattern> result = seasonalPattern.detect(transaction, matches);

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Helper method to create a mocked EmbeddingMatch with the given date and amount
     */
    @SuppressWarnings("unchecked")
    private EmbeddingMatch<TextSegment> createEmbeddingMatch(LocalDate date, double amount) {
        TextSegment segment = mock(TextSegment.class);
        dev.langchain4j.data.document.Metadata metadata =
                mock(dev.langchain4j.data.document.Metadata.class);

        when(segment.metadata()).thenReturn(metadata);
        when(metadata.getString("date")).thenReturn(date.toString());
        when(metadata.getDouble("amount")).thenReturn(amount);

        EmbeddingMatch<TextSegment> match = mock(EmbeddingMatch.class);
        when(match.embedded()).thenReturn(segment);
        when(match.score()).thenReturn(0.95);

        return match;
    }
}
