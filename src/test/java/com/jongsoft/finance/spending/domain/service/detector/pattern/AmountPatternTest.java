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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag("unit")
@DisplayName("Unit - Amount Patterns")
class AmountPatternTest {

    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @BeforeEach
    void setUp() {
        new EventBus(mock(ApplicationEventPublisher.class));
    }

    @Test
    @DisplayName("Detect increasing trend")
    void shouldDetectIncreasingTrend() {
        AmountPattern amountPattern = new AmountPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(150.0);

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(match(LocalDate.of(2025, 1, 1), 80.0, "Groceries"));
        matches.add(match(LocalDate.of(2025, 2, 1), 90.0, "Groceries"));
        matches.add(match(LocalDate.of(2025, 3, 1), 100.0, "Groceries"));
        matches.add(match(LocalDate.of(2025, 4, 1), 120.0, "Groceries"));

        Optional<SpendingPattern> result =
                amountPattern.detect("Groceries", may2025(), context(transaction, matches, 12));

        assertTrue(result.isPresent());
        SpendingPattern pattern = result.get();
        assertEquals(PatternType.INCREASING_TREND, pattern.getType());
        assertTrue(pattern.getMetadata().containsKey("recent_months_avg"));
        assertTrue(pattern.getMetadata().containsKey("percent_change"));
    }

    @Test
    @DisplayName("Detect decreasing trend")
    void shouldDetectDecreasingTrend() {
        AmountPattern amountPattern = new AmountPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Utilities")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(80.0);

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(match(LocalDate.of(2025, 1, 1), 150.0, "Utilities"));
        matches.add(match(LocalDate.of(2025, 2, 1), 140.0, "Utilities"));
        matches.add(match(LocalDate.of(2025, 3, 1), 130.0, "Utilities"));
        matches.add(match(LocalDate.of(2025, 4, 1), 110.0, "Utilities"));

        Optional<SpendingPattern> result =
                amountPattern.detect("Utilities", may2025(), context(transaction, matches, 12));

        assertTrue(result.isPresent());
        assertEquals(PatternType.DECREASING_TREND, result.get().getType());
    }

    @Test
    @DisplayName("Do not detect trend when amounts are stable")
    void shouldNotDetectTrendWhenAmountsAreStable() {
        AmountPattern amountPattern = new AmountPattern();

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Entertainment")).when(transaction).getMetadata();
        when(transaction.getDate()).thenReturn(LocalDate.of(2025, 5, 15));
        Account account = mock(Account.class);
        when(transaction.computeTo()).thenReturn(account);
        when(transaction.computeAmount(account)).thenReturn(100.0);

        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        matches.add(match(LocalDate.of(2025, 1, 1), 98.0, "Entertainment"));
        matches.add(match(LocalDate.of(2025, 2, 1), 102.0, "Entertainment"));
        matches.add(match(LocalDate.of(2025, 3, 1), 99.0, "Entertainment"));
        matches.add(match(LocalDate.of(2025, 4, 1), 101.0, "Entertainment"));

        assertTrue(amountPattern
                .detect("Entertainment", may2025(), context(transaction, matches, 12))
                .isEmpty());
    }
}
