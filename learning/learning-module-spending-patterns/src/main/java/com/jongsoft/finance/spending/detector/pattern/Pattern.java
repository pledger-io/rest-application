package com.jongsoft.finance.spending.detector.pattern;

import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;
import java.util.Optional;

public interface Pattern {

  Optional<SpendingPattern> detect(Transaction transaction, List<EmbeddingMatch<TextSegment>> matches);

}
