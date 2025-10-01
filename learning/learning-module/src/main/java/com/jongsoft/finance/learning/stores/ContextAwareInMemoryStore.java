package com.jongsoft.finance.learning.stores;

import static org.slf4j.LoggerFactory.getLogger;

import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.Control;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class ContextAwareInMemoryStore implements PledgerEmbeddingStore {
    private final Logger log = getLogger(ContextAwareInMemoryStore.class);

    private final Path storagePath;
    private final InMemoryEmbeddingStore<TextSegment> internalStore;
    private boolean shouldInitialize;

    private final Encryption encryption;
    private final String decryptionKey;

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
    }

    @Override
    public EmbeddingStore<TextSegment> embeddingStore() {
        return internalStore;
    }

    @Override
    public boolean shouldInitialize() {
        return shouldInitialize;
    }

    @Override
    public void close() {
        log.info("Shutting down embeddings store.");
        var storageBytes = internalStore.serializeToJson().getBytes(StandardCharsets.UTF_8);
        Control.Try(
                () -> Files.write(storagePath, encryption.encrypt(storageBytes, decryptionKey)));
    }
}
