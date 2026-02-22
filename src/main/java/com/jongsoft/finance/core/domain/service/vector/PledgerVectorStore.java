package com.jongsoft.finance.core.domain.service.vector;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public interface PledgerVectorStore extends AutoCloseable {
    EmbeddingStore<TextSegment> embeddingStore();

    boolean shouldInitialize();

    @Override
    void close();
}
