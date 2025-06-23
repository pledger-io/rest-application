package com.jongsoft.finance.spending.config;

import com.jongsoft.finance.learning.stores.EmbeddingStoreFactory;
import com.jongsoft.finance.learning.stores.PledgerEmbeddingStore;
import com.jongsoft.finance.spending.PatternVectorStore;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
@SpendingAnalyticsEnabled
class SpendingStarter {

  private final EmbeddingStoreFactory embeddingStoreFactory;

  SpendingStarter(EmbeddingStoreFactory embeddingStoreFactory) {
    this.embeddingStoreFactory = embeddingStoreFactory;
  }

  @Bean
  @PatternVectorStore
  PledgerEmbeddingStore patternVectorStore() {
    return embeddingStoreFactory.createEmbeddingStore("pattern_detector");
  }
}
