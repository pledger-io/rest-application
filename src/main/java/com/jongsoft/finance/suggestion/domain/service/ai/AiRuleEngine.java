package com.jongsoft.finance.suggestion.domain.service.ai;

import static com.jongsoft.finance.banking.types.TransactionLinkType.CATEGORY;
import static com.jongsoft.finance.banking.types.TransactionLinkType.EXPENSE;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.commands.LinkTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.TransactionCreated;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.domain.service.vector.PledgerVectorStore;
import com.jongsoft.finance.suggestion.adapter.api.SuggestionEngine;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;
import com.jongsoft.finance.suggestion.domain.service.vector.ClassificationVectorStore;
import com.jongsoft.lang.Control;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Primary
@Singleton
@Requires(env = "ai")
class AiRuleEngine implements SuggestionEngine {

    private final Logger log = LoggerFactory.getLogger(AiRuleEngine.class);
    private final ClassificationAgent classificationAgent;

    private final PledgerVectorStore classificationStore;
    private final EmbeddingModel embeddingModel;
    private final CurrentUserProvider currentUserProvider;
    private final UserProvider userProvider;

    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterProvider;

    AiRuleEngine(
            ClassificationAgent classificationAgent,
            @ClassificationVectorStore PledgerVectorStore classificationStore,
            EmbeddingModel embeddingModel,
            CurrentUserProvider currentUserProvider,
            UserProvider userProvider,
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterProvider) {
        this.classificationAgent = classificationAgent;
        this.classificationStore = classificationStore;
        this.embeddingModel = embeddingModel;
        this.currentUserProvider = currentUserProvider;
        this.userProvider = userProvider;
        this.transactionProvider = transactionProvider;
        this.filterProvider = filterProvider;
    }

    @Override
    @Timed(
            value = "learning.language-model",
            extraTags = {"action", "classify-transaction"})
    public SuggestionResult makeSuggestions(SuggestionInput transactionInput) {
        log.debug("Starting classification on {}.", transactionInput);

        var nullSafeInput = new SuggestionInput(
                transactionInput.date(),
                Optional.ofNullable(transactionInput.description()).orElse(""),
                Optional.ofNullable(transactionInput.fromAccount()).orElse(""),
                Optional.ofNullable(transactionInput.toAccount()).orElse(""),
                transactionInput.amount());

        var suggestions = classify(nullSafeInput).orElseGet(() -> fallbackToLLM(nullSafeInput));

        log.trace("Finished classification with suggestions {}.", suggestions);
        return suggestions;
    }

    @EventListener
    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"action", "startup"},
            longTask = true)
    void handleStartup(StartupEvent startupEvent) {
        log.info("Initializing classification embedding store.");
        if (classificationStore.shouldInitialize()) {
            for (UserAccount userAccount : userProvider.lookup()) {
                InternalAuthenticationEvent.authenticate(
                        userAccount.getUsername().email());
                var processingPage = 0;
                var filterApplied = filterProvider.create().ownAccounts().page(processingPage, 500);
                ResultPage<Transaction> transactionPage;
                do {
                    transactionPage = transactionProvider.lookup(filterApplied);
                    transactionPage.content().forEach(this::updateClassifications);
                    filterApplied.page(++processingPage, 500);
                    log.trace(
                            "Processed page {} of transactions for user {}.",
                            processingPage,
                            userAccount.getUsername().email());
                } while (transactionPage.hasNext());
            }
        }
    }

    @EventListener
    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"action", "transaction-update"})
    void handleClassificationChanged(LinkTransactionCommand command) {
        updateClassifications(transactionProvider.lookup(command.id()).get());
    }

    @EventListener
    @Timed(
            value = "learning.language-model.classify",
            extraTags = {"action", "transaction-create"})
    void handleTransactionAdded(TransactionCreated transactionCreated) {
        updateClassifications(
                transactionProvider.lookup(transactionCreated.transactionId()).get());
    }

    private SuggestionResult fallbackToLLM(SuggestionInput transactionInput) {
        log.debug("No embedding found for the input, falling back to LLM.");
        var suggestion = classificationAgent.classifyTransaction(
                UUID.randomUUID(),
                transactionInput.description(),
                transactionInput.fromAccount(),
                transactionInput.toAccount(),
                transactionInput.amount(),
                translateDate(transactionInput));
        return new SuggestionResult(suggestion.budget(), suggestion.category(), suggestion.tags());
    }

    private String translateDate(SuggestionInput transactionInput) {
        if (transactionInput.date() != null) {
            return transactionInput.date().format(DateTimeFormatter.BASIC_ISO_DATE);
        }

        return "";
    }

    private Optional<SuggestionResult> classify(SuggestionInput input) {
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

        var hits = classificationStore.embeddingStore().search(searchRequest).matches();
        if (hits.isEmpty()) {
            return Optional.empty();
        }

        var firstHit = hits.getFirst();
        return Optional.of(new SuggestionResult(
                firstHit.embedded().metadata().getString("budget"),
                firstHit.embedded().metadata().getString("category"),
                Arrays.asList(
                        Objects.requireNonNull(firstHit.embedded().metadata().getString("tags"))
                                .split(";"))));
    }

    private void updateClassifications(Transaction transaction) {
        log.debug("Updating categorisation for transaction {}", transaction.getId());

        var category = transaction.getMetadata().get(CATEGORY.name());
        var expense = transaction.getMetadata().get(EXPENSE.name());

        var tags = transaction.getTags().isEmpty()
                ? ""
                : transaction.getTags().reduce((left, right) -> left + ";" + right);
        var metadata = Map.of(
                "id", transaction.getId().toString(),
                "user", currentUserProvider.currentUser().getUsername().email(),
                "category",
                        Control.Option(category).map(Classifier::toString).getOrSupply(() -> ""),
                "budget", Control.Option(expense).map(Classifier::toString).getOrSupply(() -> ""),
                "tags", tags);
        var textSegment =
                TextSegment.textSegment(transaction.getDescription(), Metadata.from(metadata));

        classificationStore
                .embeddingStore()
                .removeAll(MetadataFilterBuilder.metadataKey("id")
                        .isEqualTo(transaction.getId().toString())
                        .and(MetadataFilterBuilder.metadataKey("user")
                                .isEqualTo(currentUserProvider
                                        .currentUser()
                                        .getUsername()
                                        .email())));

        classificationStore
                .embeddingStore()
                .add(embeddingModel.embed(textSegment).content(), textSegment);
    }
}
