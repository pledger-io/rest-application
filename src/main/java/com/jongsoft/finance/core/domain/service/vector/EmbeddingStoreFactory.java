package com.jongsoft.finance.core.domain.service.vector;

import com.jongsoft.finance.configuration.VectorConfiguration;
import com.jongsoft.finance.core.domain.service.Encryption;
import com.jongsoft.lang.Control;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

@Factory
public class EmbeddingStoreFactory {

    private final VectorConfiguration vectorConfiguration;
    private final DataSource dataSource;
    private final Encryption encryption;

    public EmbeddingStoreFactory(
            VectorConfiguration vectorConfiguration, @Nullable DataSource dataSource) {
        this.vectorConfiguration = vectorConfiguration;
        this.dataSource = dataSource;
        this.encryption = new Encryption();
    }

    public PledgerVectorStore createEmbeddingStore(String purpose) {
        if (vectorConfiguration.getStorageType() == VectorConfiguration.StorageType.PGSQL) {
            return new ContextAwarePgSQLStore(purpose, dataSource);
        }

        var storagePath = Path.of(vectorConfiguration.getStorage());
        if (!Files.exists(storagePath)) {
            Control.Try(() -> Files.createDirectories(storagePath));
        }

        return new ContextAwareInMemoryStore(
                storagePath.resolve(purpose + ".store"),
                encryption,
                vectorConfiguration.getPassKey());
    }
}
