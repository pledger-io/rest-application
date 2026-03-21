package com.jongsoft.finance.core.domain.service.vector;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.core.domain.service.Encryption;
import com.jongsoft.lang.Control;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ContextAwareInMemoryStore implements PledgerVectorStore {
    private final Logger log = getLogger(ContextAwareInMemoryStore.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Path storagePath;
    private final InMemoryEmbeddingStore<TextSegment> internalStore;
    private boolean shouldInitialize;

    private final Encryption encryption;
    private final String decryptionKey;
    private LocalDateTime lastSynchronization;

    public ContextAwareInMemoryStore(
            Path storagePath, Encryption encryption, String decryptionKey) {
        this.storagePath = storagePath;
        this.encryption = encryption;
        this.decryptionKey = decryptionKey;
        if (Files.exists(storagePath)) {
            log.debug("Embeddings found at '{}', loading from file.", storagePath);
            var encrypted = Control.Try(() -> Files.readAllBytes(storagePath));
            var contents = encryption.decrypt(encrypted.get(), decryptionKey);
            internalStore =
                    InMemoryEmbeddingStore.fromJson(new String(contents, StandardCharsets.UTF_8));
        } else {
            log.debug("No embeddings found at '{}', creating new store.", storagePath);
            internalStore = new InMemoryEmbeddingStore<>();
            shouldInitialize = true;
        }
        lastSynchronization = LocalDateTime.now();
    }

    @Override
    public EmbeddingStore<TextSegment> embeddingStore() {
        if (lastSynchronization.plusMinutes(10).isBefore(LocalDateTime.now())) {
            executorService.submit(this::writeToDisk);
        }
        return internalStore;
    }

    @Override
    public boolean shouldInitialize() {
        return shouldInitialize;
    }

    @Override
    public void close() {
        log.info("Shutting down embeddings store.");
        writeToDisk();
        executorService.shutdown();
    }

    private void writeToDisk() {
        lastSynchronization = LocalDateTime.now();
        var storageBytes = internalStore.serializeToJson().getBytes(StandardCharsets.UTF_8);
        Control.Try(
                () -> Files.write(storagePath, encryption.encrypt(storageBytes, decryptionKey)));
    }
}
