package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static com.jongsoft.finance.spending.domain.service.detector.pattern.PatternTestSupport.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.EventBus;
import com.jongsoft.finance.banking.domain.model.Account;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Seasonal Patterns")
class SeasonalPatternTest {

    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @BeforeEach
    void setup() {
        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Should detect summer seasonal pattern")
    void shouldDetectSummerPattern() {
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Travel")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 6, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(200.0);

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(match(LocalDate.of(2023, 6, 10), 80.0, "Travel"));
        matches.add(match(LocalDate.of(2024, 6, 12), 90.0, "Travel"));
        matches.add(match(LocalDate.of(2023, 3, 10), 50.0, "Travel"));
        matches.add(match(LocalDate.of(2024, 9, 5), 60.0, "Travel"));

        Optional<SpendingPattern> result = seasonalPattern.detect(
                "Travel", YearMonth.of(2025, 6), context(transaction, matches, 12));

        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.SEASONAL, pattern.getType());
        assertEquals("Summer", pattern.getMetadata().get("season"));
        assertTrue(pattern.getMetadata().containsKey("years_observed"));
    }

    @Test
    @DisplayName("Should not detect seasonal pattern when distribution is even")
    void shouldNotDetectSeasonalPatternWhenDistributionIsEven() {
        SeasonalPattern seasonalPattern = new SeasonalPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(100.0);

        List<EmbeddingMatch<TextSegment>> matches = List.of(
                match(LocalDate.of(2023, 5, 10), 100.0, "Groceries"),
                match(LocalDate.of(2024, 5, 12), 105.0, "Groceries"),
                match(LocalDate.of(2023, 8, 5), 95.0, "Groceries"),
                match(LocalDate.of(2024, 8, 8), 98.0, "Groceries"));

        assertTrue(seasonalPattern
                .detect("Groceries", may2025(), context(transaction, matches, 12))
                .isEmpty());
    }

    @Test
    @DisplayName("Handle insufficient prior years gracefully")
    void shouldHandleInsufficientPriorYears() {
        SeasonalPattern seasonalPattern = new SeasonalPattern();
        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Dining")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(200.0);

        List<EmbeddingMatch<TextSegment>> matches =
                List.of(match(LocalDate.of(2024, 5, 1), 75.0, "Dining"));

        assertTrue(seasonalPattern
                .detect("Dining", may2025(), context(transaction, matches, 12))
                .isEmpty());
    }
}
