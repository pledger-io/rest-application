package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.types.PatternType;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import io.micronaut.context.event.ApplicationEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Occurrence Pattern")
class OccurrencePatternTest {

    /**
     * This test class validates the `detect` method in OccurrencePattern.
     * The `detect` method identifies recurring weekly or monthly patterns in transactions.
     */
    private Map<String, ? extends Classifier> forCategory(String category) {
        return Map.of("CATEGORY", Category.create(category, ""));
    }

    @BeforeEach
    void setUp() {
        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Detect weekly pattern")
    void shouldDetectWeeklyPattern() {
        // Arrange
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Groceries")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list of embedding matches with weekly pattern (every Monday)
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add matches with dates exactly 7 days apart (weekly pattern)
        LocalDate startDate = LocalDate.of(2025, 3, 3); // A Monday
        for (int i = 0; i < 10; i++) {
            LocalDate date = startDate.plusDays(i * 7);
            matches.add(createEmbeddingMatch(date, 100.0));
        }

        // Act
        Optional<SpendingPattern> result = occurrencePattern.detect(transaction, matches);

        // Assert
        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.RECURRING_WEEKLY, pattern.getType());
        assertEquals("Groceries", pattern.getCategory());
        assertEquals(LocalDate.of(2025, 5, 1), pattern.getDetectedDate());
        assertTrue(pattern.getMetadata().containsKey("frequency"));
        assertEquals("weekly", pattern.getMetadata().get("frequency"));
        assertTrue(pattern.getMetadata().containsKey("typical_amount"));
        assertTrue(pattern.getMetadata().containsKey("vector_similarity"));
        assertTrue(pattern.getMetadata().containsKey("typical_day"));
        assertEquals(
                DayOfWeek.MONDAY.toString(),
                pattern.getMetadata().get("typical_day").toString());
    }

    @Test
    @DisplayName("Detect monthly pattern")
    void shouldDetectMonthlyPattern() {
        // Arrange
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Rent")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list of embedding matches with monthly pattern (1st of each month)
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add matches with dates exactly 1 month apart (monthly pattern)
        for (int i = 1; i <= 6; i++) {
            LocalDate date = LocalDate.of(2024, i + 6, 1);
            matches.add(createEmbeddingMatch(date, 1000.0));
        }
        for (int i = 1; i <= 5; i++) {
            LocalDate date = LocalDate.of(2025, i, 1);
            matches.add(createEmbeddingMatch(date, 1000.0));
        }

        // Act
        Optional<SpendingPattern> result = occurrencePattern.detect(transaction, matches);

        // Assert
        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.RECURRING_MONTHLY, pattern.getType());
        assertEquals("Rent", pattern.getCategory());
        assertEquals(LocalDate.of(2025, 5, 1), pattern.getDetectedDate());
        assertTrue(pattern.getMetadata().containsKey("frequency"));
        assertEquals("monthly", pattern.getMetadata().get("frequency"));
    }

    @Test
    @DisplayName("Do not detect pattern with irregular intervals")
    void shouldNotDetectPatternWithIrregularIntervals() {
        // Arrange
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Entertainment")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list of embedding matches with irregular intervals
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Add matches with irregular intervals
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 1, 5), 50.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 1, 20), 60.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 3), 45.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 2, 28), 55.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 3, 10), 65.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 2), 70.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 25), 50.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 8), 60.0));

        // Act
        Optional<SpendingPattern> result = occurrencePattern.detect(transaction, matches);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle null matches gracefully")
    void shouldHandleEmptyMatches() {
        // Arrange
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Travel")).when(transaction).getMetadata();

        // Create an empty list of embedding matches
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

        // Act
        Optional<SpendingPattern> result = occurrencePattern.detect(transaction, matches);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Handle insufficient matches gracefully")
    void shouldHandleInsufficientMatches() {
        // Arrange
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forCategory("Dining")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        // Create a list with only 2 matches (not enough to establish a pattern)
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 4, 1), 75.0));
        matches.add(createEmbeddingMatch(LocalDate.of(2025, 5, 1), 80.0));

        // Act
        Optional<SpendingPattern> result = occurrencePattern.detect(transaction, matches);

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
