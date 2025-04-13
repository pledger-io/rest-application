package com.jongsoft.finance.llm.stores;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.llm.configuration.AiConfiguration;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.security.Encryption;
import com.jongsoft.lang.Control;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@AiEnabled
@Singleton
public class ClassificationEmbeddingStore {

    private final Logger logger = LoggerFactory.getLogger(ClassificationEmbeddingStore.class);

    private final AiConfiguration aiConfiguration;
    private EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    private final EmbeddingStoreFiller embeddingStoreFiller;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionProvider transactionProvider;
    private final ExecutorService executorService;
    private final Encryption encryption = new Encryption();

    ClassificationEmbeddingStore(TransactionProvider transactionProvider,
                                 AiConfiguration aiConfiguration,
                                 EmbeddingStoreFiller embeddingStoreFiller,
                                 CurrentUserProvider currentUserProvider,
                                 @AiEnabled.AiExecutor ExecutorService executorService) {
        this.aiConfiguration = aiConfiguration;
        this.transactionProvider = transactionProvider;
        this.embeddingStoreFiller = embeddingStoreFiller;
        this.currentUserProvider = currentUserProvider;
        this.executorService = executorService;
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    public Optional<SuggestionResult> classify(SuggestionInput input) {
        var textSegment = TextSegment.from(input.description());

        var searchRequest = EmbeddingSearchRequest.builder()
                .filter(MetadataFilterBuilder.metadataKey("user").isEqualTo(currentUserProvider.currentUser().getUsername().email())
                        .and(
                                // Filter applied to only match something with filled category, budget or tags
                                MetadataFilterBuilder.metadataKey("category").isNotEqualTo("")
                                        .or(MetadataFilterBuilder.metadataKey("budget").isNotEqualTo(""))
                                        .or(MetadataFilterBuilder.metadataKey("tags").isNotEqualTo(""))))
                .queryEmbedding(embeddingModel.embed(textSegment).content())
                .maxResults(1)
                .minScore(.8)
                .build();

        var hits = embeddingStore.search(searchRequest).matches();
        if (hits.isEmpty()) {
            return Optional.empty();
        }

        var firstHit = hits.getFirst();
        return Optional.of(convert(firstHit.embedded().metadata()));
    }

    @EventListener
    void handleShutdown(ShutdownEvent shutdownEvent) {
        logger.info("Shutting down classification embedding store.");
        if (embeddingStore instanceof InMemoryEmbeddingStore<TextSegment> memoryEmbeddingStore) {
            if (!Files.exists(Path.of(aiConfiguration.getVectors().getClassificationStore()).getParent())) {
                Control.Try(() -> Files.createDirectories(Path.of(aiConfiguration.getVectors().getClassificationStore()).getParent()));
            }

            logger.trace("Encrypting and saving classification embeddings to file.");
            var contents = memoryEmbeddingStore.serializeToJson();
            var encrypted = encryption.encrypt(contents.getBytes(StandardCharsets.UTF_8), aiConfiguration.getVectors().getPassKey());
            Control.Try(() -> Files.write(Path.of(aiConfiguration.getVectors().getClassificationStore()), encrypted));
        }
    }

    @EventListener
    void handleApplicationStart(StartupEvent startupEvent) {
        logger.info("Starting the classification embedding store.");
        if (Files.exists(Path.of(aiConfiguration.getVectors().getClassificationStore()))) {
            logger.debug("Classification embeddings found, loading from file.");
            var encrypted = Control.Try(() -> Files.readAllBytes(Path.of(aiConfiguration.getVectors().getClassificationStore())));
            var contents = encryption.decrypt(encrypted.get(), aiConfiguration.getVectors().getPassKey());
            embeddingStore = InMemoryEmbeddingStore.fromJson(new String(contents, StandardCharsets.UTF_8));
        } else {
            logger.debug("No existing classification embeddings found, pre-filling store with existing transactions.");
            embeddingStore = new InMemoryEmbeddingStore<>();
            embeddingStoreFiller.consumeTransactions(this::updateClassifications);
        }
    }

    @BusinessEventListener
    void handleClassificationChanged(LinkTransactionCommand command) {
        executorService.submit(() -> updateClassifications(transactionProvider.lookup(command.id()).get()));
    }

    private SuggestionResult convert(Metadata metadata) {
        return new SuggestionResult(
                metadata.getString("budget"),
                metadata.getString("category"),
                Arrays.asList(metadata.getString("tags").split(";")));
    }

    private void updateClassifications(Transaction transaction) {
        logger.trace("Updating categorisation for transaction {}", transaction.getId());

        var tags = transaction.getTags().isEmpty() ? "" : transaction.getTags().reduce((left, right) -> left + ";" + right);
        var metadata = Map.of(
                "id", transaction.getId().toString(),
                "user", currentUserProvider.currentUser().getUsername().email(),
                "category", Control.Option(transaction.getCategory()).getOrSupply(() -> ""),
                "budget", Control.Option(transaction.getBudget()).getOrSupply(() -> ""),
                "tags", tags);
        var textSegment = TextSegment.textSegment(transaction.getDescription(), Metadata.from(metadata));

        embeddingStore.removeAll(
                MetadataFilterBuilder.metadataKey("id")
                        .isEqualTo(transaction.getId().toString())
                        .and(MetadataFilterBuilder.metadataKey("user")
                                .isEqualTo(currentUserProvider.currentUser().getUsername().email())));

        embeddingStore.add(embeddingModel.embed(textSegment).content(), textSegment);
    }
}
