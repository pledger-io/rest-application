package com.jongsoft.finance.learning.stores;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import javax.sql.DataSource;

public class ContextAwarePgSQLStore implements PledgerEmbeddingStore {
  private PgVectorEmbeddingStore internalStore;
  private final String tableName;
  private final DataSource dataSource;

  ContextAwarePgSQLStore(String tableName, DataSource dataSource) {
    this.tableName = tableName;
    this.dataSource = dataSource;
  }

  @Override
  public EmbeddingStore<TextSegment> embeddingStore() {
    if (internalStore == null) {
      internalStore = PgVectorEmbeddingStore.datasourceBuilder()
          .datasource(dataSource)
          .table("embedding_" + tableName)
          .dimension(384) // copied from the
          // AllMiniLmL6V2EmbeddingModel.knownDimension
          .createTable(true)
          .build();
    }
    return internalStore;
  }

  @Override
  public boolean shouldInitialize() {
    return false;
  }

  @Override
  public void close() {
    // no action needed for the PgVector store
  }
}
