package com.jongsoft.finance.spending.domain.service.detector.pattern;

import static com.jongsoft.finance.banking.types.TransactionLinkType.CATEGORY;

import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;
import java.util.Optional;

public interface Pattern {

    Optional<SpendingPattern> detect(
            Transaction transaction, List<EmbeddingMatch<TextSegment>> matches);

    default String getCategory(Transaction transaction) {
        return transaction.getMetadata().get(CATEGORY.name()).toString();
    }
}
