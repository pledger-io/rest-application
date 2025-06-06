package com.jongsoft.finance.llm.stores;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

interface MicronautEmbeddingStore extends AutoCloseable {

  EmbeddingStore<TextSegment> embeddingStore();

  EmbeddingStoreFiller embeddingStoreFiller();

  boolean shouldInitialize();

  @Override
  void close();
}
