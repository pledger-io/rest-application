package com.jongsoft.finance.learning.stores;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public interface PledgerEmbeddingStore extends AutoCloseable {
  EmbeddingStore<TextSegment> embeddingStore();

  boolean shouldInitialize();

  @Override
  void close();
}
