package com.jongsoft.finance.spending.domain.service.detector;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.commands.LinkTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.TransactionCreated;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.ResultPage;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.domain.service.vector.PledgerVectorStore;
import com.jongsoft.finance.spending.domain.model.SpendingPattern;
import com.jongsoft.finance.spending.domain.service.SpendingAnalyticsEnabled;
import com.jongsoft.finance.spending.domain.service.detector.pattern.AmountPattern;
import com.jongsoft.finance.spending.domain.service.detector.pattern.OccurrencePattern;
import com.jongsoft.finance.spending.domain.service.detector.pattern.Pattern;
import com.jongsoft.finance.spending.domain.service.detector.pattern.SeasonalPattern;
import com.jongsoft.finance.spending.domain.service.vector.PatternVectorStore;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;

import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@SpendingAnalyticsEnabled
class PatternDetector implements Detector<SpendingPattern> {
    // Threshold for similarity matching
    private static final double SIMILARITY_THRESHOLD = 0.9;
    private static final int MIN_TRANSACTIONS_FOR_PATTERN = 3;

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatternDetector.class);

    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterProvider;
    private final CurrentUserProvider currentUserProvider;
    private final UserProvider userProvider;

    private final EmbeddingModel embeddingModel;
    private final PledgerVectorStore patternVectorStore;

    private final List<Pattern> patterns;

    private boolean hasInitialized;

    PatternDetector(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterProvider,
            CurrentUserProvider currentUserProvider,
            UserProvider userProvider,
            @PatternVectorStore PledgerVectorStore patternVectorStore) {
        this.transactionProvider = transactionProvider;
        this.currentUserProvider = currentUserProvider;
        this.userProvider = userProvider;
        this.patternVectorStore = patternVectorStore;
        this.filterProvider = filterProvider;
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.patterns =
                List.of(new OccurrencePattern(), new AmountPattern(), new SeasonalPattern());
    }

    @EventListener
    void handleStartup(StartupEvent startupEvent) {
        if (patternVectorStore.shouldInitialize()) {
            log.debug("Initially filling pattern vector store with transactions.");
            for (UserAccount userAccount : userProvider.lookup()) {
                InternalAuthenticationEvent.authenticate(
                        userAccount.getUsername().email());
                var processingPage = 0;
                var filterApplied = filterProvider.create().ownAccounts().page(processingPage, 500);
                ResultPage<Transaction> transactionPage;
                do {
                    transactionPage = transactionProvider.lookup(filterApplied);
                    transactionPage.content().forEach(this::indexTransaction);
                    filterApplied.page(++processingPage, 500);
                    log.trace(
                            "Processed page {} of transactions for user {}.",
                            processingPage,
                            userAccount.getUsername().email());
                } while (transactionPage.hasNext());
            }
        }
        hasInitialized = true;
    }

    @EventListener
    void handleShutdown(ShutdownEvent shutdownEvent) {
        log.info("Shutting down pattern detector embedding store.");
        patternVectorStore.close();
    }

    @EventListener
    void handleClassificationChanged(LinkTransactionCommand command) {
        indexTransaction(transactionProvider.lookup(command.id()).get());
    }

    @EventListener
    void handleTransactionAdded(TransactionCreated transactionCreated) {
        indexTransaction(
                transactionProvider.lookup(transactionCreated.transactionId()).get());
    }

    @Override
    public boolean readyForAnalysis() {
        return hasInitialized;
    }

    @Override
    public void updateBaseline(YearMonth forMonth) {
        // no filling is needed
    }

    @Override
    public void analysisCompleted() {
        // no action needed
    }

    @Override
    public List<SpendingPattern> detect(Transaction transaction) {
        // Skip transactions without a category or budget
        if (!transaction.getMetadata().containsKey("CATEGORY")
                && !transaction.getMetadata().containsKey("EXPENSE")) {
            return List.of();
        }

        var segment = createTextSegment(transaction);

        // Search for similar transactions
        var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(segment).content())
                .filter(MetadataFilterBuilder.metadataKey("user")
                        .isEqualTo(
                                currentUserProvider.currentUser().getUsername().email())
                        .and(MetadataFilterBuilder.metadataKey("date")
                                .isBetween(
                                        transaction.getDate().minusMonths(3).toString(),
                                        transaction.getDate().toString())))
                .maxResults(150)
                .minScore(SIMILARITY_THRESHOLD)
                .build();

        var matches = patternVectorStore.embeddingStore().search(searchRequest).matches();

        if (matches.size() >= MIN_TRANSACTIONS_FOR_PATTERN) {
            return patterns.stream()
                    .map(p -> p.detect(transaction, matches))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }

        return List.of();
    }

    private void indexTransaction(Transaction transaction) {
        TextSegment segment = createTextSegment(transaction);

        // Remove any existing entries for this transaction
        patternVectorStore
                .embeddingStore()
                .removeAll(MetadataFilterBuilder.metadataKey("id")
                        .isEqualTo(transaction.getId().toString())
                        .and(MetadataFilterBuilder.metadataKey("user")
                                .isEqualTo(currentUserProvider
                                        .currentUser()
                                        .getUsername()
                                        .email())));

        // Add the transaction to the vector store
        patternVectorStore.embeddingStore().add(embeddingModel.embed(segment).content(), segment);
    }

    private TextSegment createTextSegment(Transaction transaction) {
        var expense = transaction.getMetadata().get("EXPENSE");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("id", transaction.getId().toString());
        metadata.put("user", currentUserProvider.currentUser().getUsername().email());
        metadata.put("date", transaction.getDate().toString());
        metadata.put(
                "amount", String.valueOf(transaction.computeAmount(transaction.computeFrom())));
        metadata.put("budget", expense != null ? expense.toString() : "");

        // Create a rich text representation of the transaction
        String text =
                String.format("%s - %s", metadata.get("budget"), transaction.getDescription());

        return TextSegment.textSegment(text, Metadata.from(metadata));
    }
}
