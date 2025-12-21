package com.jongsoft.finance.spending.detector.pattern;

import static com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand.LinkType.CATEGORY;

import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Category;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;
import java.util.Optional;

public interface Pattern {

    Optional<SpendingPattern> detect(
            Transaction transaction, List<EmbeddingMatch<TextSegment>> matches);

    default String getCategory(Transaction transaction) {
        return ((Category) transaction.getMetadata().get(CATEGORY.name())).getLabel();
    }
}
