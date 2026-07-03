package com.jongsoft.finance.spending.domain.service.detector.pattern;

import com.jongsoft.finance.banking.domain.model.Transaction;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

/** Historic and current-month data used for category-level pattern detection. */
public record PatternMonthContext(
        List<Transaction> monthTransactions,
        List<EmbeddingMatch<TextSegment>> historicMatches,
        int lookbackMonths) {}
