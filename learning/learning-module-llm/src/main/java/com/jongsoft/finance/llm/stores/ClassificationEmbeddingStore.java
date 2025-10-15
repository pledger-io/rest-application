package com.jongsoft.finance.llm.stores;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.learning.stores.EmbeddingStoreFiller;
import com.jongsoft.finance.learning.stores.PledgerEmbeddingStore;
import com.jongsoft.finance.llm.AiEnabled;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.messaging.notifications.TransactionCreated;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Singleton
@AiEnabled
public class ClassificationEmbeddingStore {

    private final Logger logger = LoggerFactory.getLogger(ClassificationEmbeddingStore.class);

    private final PledgerEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStoreFiller embeddingStoreFiller;

    private final CurrentUserProvider currentUserProvider;
    private final TransactionProvider transactionProvider;

    ClassificationEmbeddingStore(
            @AiEnabled.ClassificationAgent PledgerEmbeddingStore embeddingStore,
            EmbeddingStoreFiller embeddingStoreFiller,
            TransactionProvider transactionProvider,
            CurrentUserProvider currentUserProvider) {
        this.embeddingStore = embeddingStore;
        this.embeddingStoreFiller = embeddingStoreFiller;
        this.transactionProvider = transactionProvider;
        this.currentUserProvider = currentUserProvider;
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"perform-classify"})
    public Optional<SuggestionResult> classify(SuggestionInput input) {
        var textSegment = TextSegment.from(input.description());

        var searchRequest = EmbeddingSearchRequest.builder()
                .filter(MetadataFilterBuilder.metadataKey("user")
                        .isEqualTo(
                                currentUserProvider.currentUser().getUsername().email())
                        .and(
                                // Filter applied to only match something with
                                // filled category, budget or
                                // tags
                                MetadataFilterBuilder.metadataKey("category")
                                        .isNotEqualTo("")
                                        .or(MetadataFilterBuilder.metadataKey("budget")
                                                .isNotEqualTo(""))
                                        .or(MetadataFilterBuilder.metadataKey("tags")
                                                .isNotEqualTo(""))))
                .queryEmbedding(embeddingModel.embed(textSegment).content())
                .maxResults(1)
                .minScore(.8)
                .build();

        var hits = embeddingStore.embeddingStore().search(searchRequest).matches();
        if (hits.isEmpty()) {
            return Optional.empty();
        }

        var firstHit = hits.getFirst();
        return Optional.of(convert(firstHit.embedded().metadata()));
    }

    @EventListener
    void handleStartup(StartupEvent startupEvent) {
        logger.info("Initializing classification embedding store.");
        if (embeddingStore.shouldInitialize()) {
            embeddingStoreFiller.consumeTransactions(this::updateClassifications);
        }
    }

    @EventListener
    void handleShutdown(ShutdownEvent shutdownEvent) {
        logger.info("Shutting down classification embedding store.");
        embeddingStore.close();
    }

    @EventListener
    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"transaction-update"})
    void handleClassificationChanged(LinkTransactionCommand command) {
        updateClassifications(transactionProvider.lookup(command.id()).get());
    }

    @EventListener
    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"transaction-create"})
    void handleTransactionAdded(TransactionCreated transactionCreated) {
        updateClassifications(
                transactionProvider.lookup(transactionCreated.transactionId()).get());
    }

    private SuggestionResult convert(Metadata metadata) {
        return new SuggestionResult(
                metadata.getString("budget"),
                metadata.getString("category"),
                Arrays.asList(metadata.getString("tags").split(";")));
    }

    private void updateClassifications(Transaction transaction) {
        logger.trace("Updating categorisation for transaction {}", transaction.getId());

        var tags = transaction.getTags().isEmpty()
                ? ""
                : transaction.getTags().reduce((left, right) -> left + ";" + right);
        var metadata = Map.of(
                "id", transaction.getId().toString(),
                "user", currentUserProvider.currentUser().getUsername().email(),
                "category", Control.Option(transaction.getCategory()).getOrSupply(() -> ""),
                "budget", Control.Option(transaction.getBudget()).getOrSupply(() -> ""),
                "tags", tags);
        var textSegment =
                TextSegment.textSegment(transaction.getDescription(), Metadata.from(metadata));

        embeddingStore
                .embeddingStore()
                .removeAll(MetadataFilterBuilder.metadataKey("id")
                        .isEqualTo(transaction.getId().toString())
                        .and(MetadataFilterBuilder.metadataKey("user")
                                .isEqualTo(currentUserProvider
                                        .currentUser()
                                        .getUsername()
                                        .email())));

        embeddingStore
                .embeddingStore()
                .add(embeddingModel.embed(textSegment).content(), textSegment);
    }
}
