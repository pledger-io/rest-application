package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static com.jongsoft.finance.spending.domain.service.detector.pattern.PatternTestSupport.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
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

    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @BeforeEach
    void setUp() {
        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Detect weekly pattern")
    void shouldDetectWeeklyPattern() {
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2025, 3, 3);
        for (int i = 0; i < 10; i++) {
            matches.add(match(startDate.plusDays(i * 7L), 100.0, "Groceries"));
        }

        Optional<SpendingPattern> result =
                occurrencePattern.detect("Groceries", may2025(), context(transaction, matches, 12));

        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.RECURRING_WEEKLY, pattern.getType());
        assertEquals("Groceries", pattern.getCategory());
        assertEquals(LocalDate.of(2025, 5, 1), pattern.getDetectedDate());
        assertEquals("weekly", pattern.getMetadata().get("frequency"));
        assertEquals(
                DayOfWeek.MONDAY.toString(),
                pattern.getMetadata().get("typical_day").toString());
    }

    @Test
    @DisplayName("Detect monthly pattern")
    void shouldDetectMonthlyPattern() {
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Rent")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            matches.add(match(LocalDate.of(2024, i + 6, 1), 1000.0, "Rent"));
        }
        for (int i = 1; i <= 4; i++) {
            matches.add(match(LocalDate.of(2025, i, 1), 1000.0, "Rent"));
        }

        Optional<SpendingPattern> result =
                occurrencePattern.detect("Rent", may2025(), context(transaction, matches, 12));

        assertTrue(result.isPresent());
        assertEquals(PatternType.RECURRING_MONTHLY, result.get().getType());
        assertEquals("monthly", result.get().getMetadata().get("frequency"));
    }

    @Test
    @DisplayName("Do not detect pattern with irregular intervals")
    void shouldNotDetectPatternWithIrregularIntervals() {
        OccurrencePattern occurrencePattern = new OccurrencePattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Entertainment")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        List<EmbeddingMatch<TextSegment>> matches = List.of(
                match(LocalDate.of(2025, 1, 5), 50.0, "Entertainment"),
                match(LocalDate.of(2025, 1, 20), 60.0, "Entertainment"),
                match(LocalDate.of(2025, 2, 3), 45.0, "Entertainment"),
                match(LocalDate.of(2025, 2, 28), 55.0, "Entertainment"),
                match(LocalDate.of(2025, 3, 10), 65.0, "Entertainment"),
                match(LocalDate.of(2025, 4, 2), 70.0, "Entertainment"),
                match(LocalDate.of(2025, 4, 25), 50.0, "Entertainment"),
                match(LocalDate.of(2025, 5, 8), 60.0, "Entertainment"));

        assertTrue(occurrencePattern
                .detect("Entertainment", may2025(), context(transaction, matches, 12))
                .isEmpty());
    }

    @Test
    @DisplayName("Handle insufficient matches gracefully")
    void shouldHandleInsufficientMatches() {
        OccurrencePattern occurrencePattern = new OccurrencePattern();
        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Dining")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));

        List<EmbeddingMatch<TextSegment>> matches = List.of(
                match(LocalDate.of(2025, 4, 1), 75.0, "Dining"),
                match(LocalDate.of(2025, 5, 1), 80.0, "Dining"));

        assertTrue(occurrencePattern
                .detect("Dining", may2025(), context(transaction, matches, 12))
                .isEmpty());
    }
}
