package com.jongsoft.finance.llm.stores;

import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.configuration.AiConfiguration;
import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.Control;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requirements;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.slf4j.LoggerFactory.getLogger;

@Factory
@AiEnabled
class EmbeddingStoreFactory {

    private final Logger log = getLogger(EmbeddingStoreFactory.class);

    private final EmbeddingStoreFiller embeddingStoreFiller;
    private final DataSource dataSource;
    private final AiConfiguration configuration;
    private final Encryption encryption;

    EmbeddingStoreFactory(EmbeddingStoreFiller embeddingStoreFiller, @Nullable DataSource dataSource, AiConfiguration configuration) {
        this.embeddingStoreFiller = embeddingStoreFiller;
        this.dataSource = dataSource;
        this.configuration = configuration;
        this.encryption = new Encryption();
    }

    private class ContextAwareInMemoryStore implements MicronautEmbeddingStore {
        private final Path storagePath;
        private final InMemoryEmbeddingStore<TextSegment> internalStore;
        private boolean shouldInitialize;

        private ContextAwareInMemoryStore(Path storagePath) {
            this.storagePath = storagePath;
            if (Files.exists(storagePath)) {
                log.debug("Embeddings found at '{}', loading from file.", storagePath);
                var encrypted = Control.Try(() -> Files.readAllBytes(storagePath));
                var contents = encryption.decrypt(encrypted.get(), configuration.getVectors().getPassKey());
                internalStore = InMemoryEmbeddingStore.fromJson(new String(contents, StandardCharsets.UTF_8));
            } else {
                log.debug("No embeddings found at '{}', creating new store.", storagePath);
                internalStore = new InMemoryEmbeddingStore<>();
                shouldInitialize = true;
            }
        }

        @Override
        public boolean shouldInitialize() {
            return shouldInitialize;
        }

        @Override
        public InMemoryEmbeddingStore<TextSegment> embeddingStore() {
            return internalStore;
        }

        @Override
        public EmbeddingStoreFiller embeddingStoreFiller() {
            return embeddingStoreFiller;
        }

        public void close() {
            log.info("Shutting down embeddings store.");
            var storageBytes = internalStore.serializeToJson().getBytes(StandardCharsets.UTF_8);
            Control.Try(() -> Files.write(storagePath, encryption.encrypt(storageBytes, configuration.getVectors().getPassKey())));
        }
    }

    private class ContextAwarePgSQLStore implements MicronautEmbeddingStore {
        private PgVectorEmbeddingStore internalStore;
        private final String tableName;

        private ContextAwarePgSQLStore(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public EmbeddingStore<TextSegment> embeddingStore() {
            if (internalStore == null) {
                internalStore = PgVectorEmbeddingStore.datasourceBuilder()
                        .datasource(dataSource)
                        .table("embedding_" + tableName)
                        .dimension(384) // copied from the AllMiniLmL6V2EmbeddingModel.knownDimension
                        .createTable(true)
                        .build();
            }
            return internalStore;
        }

        @Override
        public EmbeddingStoreFiller embeddingStoreFiller() {
            return embeddingStoreFiller;
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

    @Bean
    @AiEnabled.ClassificationAgent
    @Requirements(@Requires(missingBeans = MicronautEmbeddingStore.class))
    public MicronautEmbeddingStore classificationInMemory() {
        var storagePath = Path.of(configuration.getVectors().getStorage());
        if (!Files.exists(storagePath)) {
            Control.Try(() -> Files.createDirectories(storagePath));
        }

        return new ContextAwareInMemoryStore(storagePath.resolve("classification.store"));
    }

    @Bean
    @AiEnabled.ClassificationAgent
    @Requires(env = "psql")
    public MicronautEmbeddingStore classificationPgsql() {
        return new ContextAwarePgSQLStore("classification");
    }
}
