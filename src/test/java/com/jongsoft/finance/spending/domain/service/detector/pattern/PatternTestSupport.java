package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jongsoft.finance.banking.domain.model.Transaction;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

final class PatternTestSupport {

    private PatternTestSupport() {}

    static PatternMonthContext context(
            Transaction transaction,
            List<EmbeddingMatch<TextSegment>> matches,
            int lookbackMonths) {
        return new PatternMonthContext(
                transaction != null ? List.of(transaction) : List.of(), matches, lookbackMonths);
    }

    @SuppressWarnings("unchecked")
    static EmbeddingMatch<TextSegment> match(LocalDate date, double amount, String expense) {
        TextSegment segment = mock(TextSegment.class);
        Metadata metadata = mock(Metadata.class);

        when(segment.metadata()).thenReturn(metadata);
        when(metadata.getString("date")).thenReturn(date.toString());
        when(metadata.getDouble("amount")).thenReturn(amount);
        when(metadata.getString("expense")).thenReturn(expense);

        EmbeddingMatch<TextSegment> embeddingMatch = mock(EmbeddingMatch.class);
        when(embeddingMatch.embedded()).thenReturn(segment);
        when(embeddingMatch.score()).thenReturn(0.95);
        return embeddingMatch;
    }

    static YearMonth may2025() {
        return YearMonth.of(2025, 5);
    }
}
