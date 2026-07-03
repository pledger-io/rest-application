package com.jongsoft.finance.spending.domain.service.detector;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.commands.LinkTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.TransactionCreated;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
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
import com.jongsoft.finance.spending.domain.service.detector.pattern.PatternMonthContext;
import com.jongsoft.finance.spending.domain.service.detector.pattern.SeasonalPattern;
import com.jongsoft.finance.spending.domain.service.vector.PatternVectorStore;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;

import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Singleton
@SpendingAnalyticsEnabled
class PatternDetector implements Detector<SpendingPattern> {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PatternDetector.class);

    private final TransactionProvider transactionProvider;
    private final FilterProvider<TransactionProvider.FilterCommand> filterProvider;
    private final CurrentUserProvider currentUserProvider;
    private final UserProvider userProvider;
    private final SpendingAnalysisConfiguration settings;
    private final EmbeddingModel embeddingModel;
    private final PledgerVectorStore patternVectorStore;
    private final List<Pattern> patterns;

    private volatile boolean hasInitialized;

    PatternDetector(
            TransactionProvider transactionProvider,
            FilterProvider<TransactionProvider.FilterCommand> filterProvider,
            CurrentUserProvider currentUserProvider,
            UserProvider userProvider,
            SpendingAnalysisConfiguration settings,
            EmbeddingModel embeddingModel,
            @PatternVectorStore PledgerVectorStore patternVectorStore) {
        this.transactionProvider = transactionProvider;
        this.currentUserProvider = currentUserProvider;
        this.userProvider = userProvider;
        this.settings = settings;
        this.patternVectorStore = patternVectorStore;
        this.filterProvider = filterProvider;
        this.embeddingModel = embeddingModel;
        this.patterns =
                List.of(new OccurrencePattern(), new AmountPattern(), new SeasonalPattern());
    }

    @EventListener
    void handleStartup(StartupEvent startupEvent) {
        if (!patternVectorStore.shouldInitialize()) {
            hasInitialized = true;
            return;
        }

        log.debug("Initially filling pattern vector store with transactions.");
        var indexingTasks = new ArrayList<CompletableFuture<Void>>();
        for (UserAccount userAccount : userProvider.lookup()) {
            indexingTasks.add(CompletableFuture.runAsync(
                    () -> {
                        InternalAuthenticationEvent.authenticate(
                                userAccount.getUsername().email());
                        var processingPage = 0;
                        var filterApplied =
                                filterProvider.create().ownAccounts().page(processingPage, 500);
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
                    },
                    executorService));
        }

        CompletableFuture.allOf(indexingTasks.toArray(CompletableFuture[]::new))
                .whenComplete((_, error) -> {
                    if (error != null) {
                        log.error("Pattern vector store initialization failed", error);
                    }
                    hasInitialized = true;
                });
    }

    @EventListener
    void handleShutdown(ShutdownEvent shutdownEvent) {
        log.info("Shutting down pattern detector embedding store.");
        patternVectorStore.close();
        executorService.shutdown();
    }

    @EventListener
    void handleClassificationChanged(LinkTransactionCommand command) {
        transactionProvider.lookup(command.id()).ifPresent(this::indexTransaction);
    }

    @EventListener
    void handleTransactionAdded(TransactionCreated transactionCreated) {
        transactionProvider
                .lookup(transactionCreated.transactionId())
                .ifPresent(this::indexTransaction);
    }

    @Override
    public boolean readyForAnalysis() {
        return hasInitialized;
    }

    @Override
    public void updateBaseline(YearMonth forMonth) {
        // Vector index is maintained incrementally; month context is built during detectForMonth.
    }

    @Override
    public void analysisCompleted() {
        // no action needed
    }

    @Override
    public List<SpendingPattern> detect(Transaction transaction) {
        return List.of();
    }

    @Override
    public List<SpendingPattern> detectForMonth(
            YearMonth forMonth, List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return List.of();
        }

        return groupByExpense(transactions).entrySet().stream()
                .flatMap(entry ->
                        detectForCategory(entry.getKey(), forMonth, entry.getValue()).stream())
                .toList();
    }

    private List<SpendingPattern> detectForCategory(
            String category, YearMonth forMonth, List<Transaction> monthTransactions) {
        var historicMatches = searchHistoricMatches(category, forMonth, monthTransactions);
        if (historicMatches.size() < settings.patternMinMatches()) {
            return List.of();
        }

        var context = new PatternMonthContext(
                monthTransactions, historicMatches, settings.patternLookbackMonths());

        return patterns.stream()
                .map(pattern -> pattern.detect(category, forMonth, context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private List<EmbeddingMatch<TextSegment>> searchHistoricMatches(
            String category, YearMonth forMonth, List<Transaction> monthTransactions) {
        LocalDate lookbackStart =
                forMonth.minusMonths(settings.patternLookbackMonths()).atDay(1);
        LocalDate lookbackEnd = forMonth.minusMonths(1).atEndOfMonth();

        var querySegment = monthTransactions.isEmpty()
                ? createCategoryQuerySegment(category)
                : createTextSegment(monthTransactions.getFirst());

        var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(querySegment).content())
                .filter(MetadataFilterBuilder.metadataKey("user")
                        .isEqualTo(
                                currentUserProvider.currentUser().getUsername().email())
                        .and(MetadataFilterBuilder.metadataKey("expense").isEqualTo(category))
                        .and(MetadataFilterBuilder.metadataKey("date")
                                .isBetween(lookbackStart.toString(), lookbackEnd.toString())))
                .maxResults(150)
                .minScore(settings.patternSimilarityThreshold())
                .build();

        return patternVectorStore.embeddingStore().search(searchRequest).matches().stream()
                .filter(match -> category.equals(match.embedded().metadata().getString("expense")))
                .toList();
    }

    private TextSegment createCategoryQuerySegment(String category) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("expense", category);
        metadata.put("user", currentUserProvider.currentUser().getUsername().email());
        return TextSegment.textSegment(category + " - ", Metadata.from(metadata));
    }

    private Map<String, List<Transaction>> groupByExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(SpendingCategoryResolver::hasCategory)
                .collect(Collectors.groupingBy(SpendingCategoryResolver::resolve));
    }

    private void indexTransaction(Transaction transaction) {
        if (Pattern.resolveCategory(transaction) == null) {
            return;
        }

        TextSegment segment = createTextSegment(transaction);

        patternVectorStore
                .embeddingStore()
                .removeAll(MetadataFilterBuilder.metadataKey("id")
                        .isEqualTo(transaction.getId().toString())
                        .and(MetadataFilterBuilder.metadataKey("user")
                                .isEqualTo(currentUserProvider
                                        .currentUser()
                                        .getUsername()
                                        .email())));

        patternVectorStore.embeddingStore().add(embeddingModel.embed(segment).content(), segment);
    }

    private TextSegment createTextSegment(Transaction transaction) {
        String expenseName = Pattern.resolveCategory(transaction);
        Objects.requireNonNull(expenseName);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("id", transaction.getId().toString());
        metadata.put("user", currentUserProvider.currentUser().getUsername().email());
        metadata.put("date", transaction.getDate().toString());
        metadata.put("amount", String.valueOf(transaction.computeAmount(transaction.computeTo())));
        metadata.put("expense", expenseName);
        metadata.put("budget", expenseName);

        String text = String.format("%s - %s", expenseName, transaction.getDescription());
        return TextSegment.textSegment(text, Metadata.from(metadata));
    }
}
