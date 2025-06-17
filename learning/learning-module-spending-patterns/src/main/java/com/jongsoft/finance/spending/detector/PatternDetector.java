package com.jongsoft.finance.spending.detector;

import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.learning.stores.EmbeddingStoreFiller;
import com.jongsoft.finance.learning.stores.PledgerEmbeddingStore;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.messaging.notifications.TransactionCreated;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.spending.Detector;
import com.jongsoft.finance.spending.PatternVectorStore;
import com.jongsoft.finance.spending.SpendingAnalyticsEnabled;
import com.jongsoft.finance.spending.detector.pattern.AmountPattern;
import com.jongsoft.finance.spending.detector.pattern.OccurrencePattern;
import com.jongsoft.finance.spending.detector.pattern.Pattern;
import com.jongsoft.finance.spending.detector.pattern.SeasonalPattern;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@SpendingAnalyticsEnabled
class PatternDetector implements Detector<SpendingPattern> {
  // Threshold for similarity matching
  private static final double SIMILARITY_THRESHOLD = 0.9;
  private static final int MIN_TRANSACTIONS_FOR_PATTERN = 3;

  private final TransactionProvider transactionProvider;
  private final CurrentUserProvider currentUserProvider;

  private final EmbeddingModel embeddingModel;
  private final PledgerEmbeddingStore patternVectorStore;

  private final List<Pattern> patterns;
  private final EmbeddingStoreFiller embeddingStoreFiller;

  PatternDetector(
      @Nullable TransactionProvider transactionProvider,
      CurrentUserProvider currentUserProvider,
      @PatternVectorStore PledgerEmbeddingStore patternVectorStore,
      EmbeddingStoreFiller embeddingStoreFiller) {
    this.transactionProvider = transactionProvider;
    this.currentUserProvider = currentUserProvider;
    this.patternVectorStore = patternVectorStore;
    this.embeddingStoreFiller = embeddingStoreFiller;
    this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    this.patterns = List.of(new OccurrencePattern(), new AmountPattern(), new SeasonalPattern());
  }

  @EventListener
  void handleStartup(StartupEvent startupEvent) {
    if (patternVectorStore.shouldInitialize()) {
      log.debug("Initially filling pattern vector store with transactions.");
      embeddingStoreFiller.consumeTransactions(this::indexTransaction);
    }
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
    indexTransaction(transactionProvider.lookup(transactionCreated.transactionId()).get());
  }

  @Override
  public boolean readyForAnalysis() {
    return embeddingStoreFiller.isDone();
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
    if (transaction.getCategory() == null && transaction.getBudget() == null) {
      return List.of();
    }

    var segment = createTextSegment(transaction);

    // Search for similar transactions
    var searchRequest =
        EmbeddingSearchRequest.builder()
            .queryEmbedding(embeddingModel.embed(segment).content())
            .filter(
                MetadataFilterBuilder.metadataKey("user")
                    .isEqualTo(currentUserProvider.currentUser().getUsername().email())
                    .and(
                        MetadataFilterBuilder.metadataKey("date")
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
        .removeAll(
            MetadataFilterBuilder.metadataKey("id")
                .isEqualTo(transaction.getId().toString())
                .and(
                    MetadataFilterBuilder.metadataKey("user")
                        .isEqualTo(currentUserProvider.currentUser().getUsername().email())));

    // Add the transaction to the vector store
    patternVectorStore.embeddingStore().add(embeddingModel.embed(segment).content(), segment);
  }

  private TextSegment createTextSegment(Transaction transaction) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("id", transaction.getId().toString());
    metadata.put("user", currentUserProvider.currentUser().getUsername().email());
    metadata.put("date", transaction.getDate().toString());
    metadata.put("amount", String.valueOf(transaction.computeAmount(transaction.computeFrom())));
    metadata.put("budget", transaction.getBudget() != null ? transaction.getBudget() : "");

    // Create a rich text representation of the transaction
    String text = String.format("%s - %s", transaction.getBudget(), transaction.getDescription());

    return TextSegment.textSegment(text, Metadata.from(metadata));
  }
}
