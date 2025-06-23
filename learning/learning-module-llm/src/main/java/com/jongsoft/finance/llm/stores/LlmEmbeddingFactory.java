package com.jongsoft.finance.llm.stores;

import com.jongsoft.finance.learning.stores.EmbeddingStoreFactory;
import com.jongsoft.finance.learning.stores.PledgerEmbeddingStore;
import com.jongsoft.finance.llm.AiEnabled;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
@AiEnabled
class LlmEmbeddingFactory {

  private final EmbeddingStoreFactory embeddingStoreFactory;

  LlmEmbeddingFactory(EmbeddingStoreFactory embeddingStoreFactory) {
    this.embeddingStoreFactory = embeddingStoreFactory;
  }

  @Bean
  @AiEnabled.ClassificationAgent
  public PledgerEmbeddingStore classificationInMemory() {
    return embeddingStoreFactory.createEmbeddingStore("classification");
  }
}
