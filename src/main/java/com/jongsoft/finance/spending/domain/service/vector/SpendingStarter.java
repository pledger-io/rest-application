package com.jongsoft.finance.spending.domain.service.vector;

import com.jongsoft.finance.core.domain.service.vector.EmbeddingStoreFactory;
import com.jongsoft.finance.core.domain.service.vector.PledgerVectorStore;
import com.jongsoft.finance.spending.domain.service.SpendingAnalyticsEnabled;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
@SpendingAnalyticsEnabled
class SpendingStarter {

    private final EmbeddingStoreFactory embeddingStoreFactory;

    SpendingStarter(EmbeddingStoreFactory embeddingStoreFactory) {
        this.embeddingStoreFactory = embeddingStoreFactory;
    }

    @Bean(preDestroy = "close")
    @PatternVectorStore
    PledgerVectorStore patternVectorStore() {
        return embeddingStoreFactory.createEmbeddingStore("pattern_detector");
    }
}
