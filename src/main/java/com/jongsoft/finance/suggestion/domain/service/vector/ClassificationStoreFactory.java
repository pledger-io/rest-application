package com.jongsoft.finance.suggestion.domain.service.vector;

import com.jongsoft.finance.core.domain.service.vector.EmbeddingStoreFactory;
import com.jongsoft.finance.core.domain.service.vector.PledgerVectorStore;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
class ClassificationStoreFactory {

    private final EmbeddingStoreFactory embeddingStoreFactory;

    ClassificationStoreFactory(EmbeddingStoreFactory embeddingStoreFactory) {
        this.embeddingStoreFactory = embeddingStoreFactory;
    }

    @Bean(preDestroy = "close")
    @ClassificationVectorStore
    PledgerVectorStore pledgerVectorStore() {
        return embeddingStoreFactory.createEmbeddingStore("classification");
    }
}
