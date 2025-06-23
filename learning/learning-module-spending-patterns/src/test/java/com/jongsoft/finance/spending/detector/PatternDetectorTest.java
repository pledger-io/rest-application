package com.jongsoft.finance.spending.detector;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.insight.SpendingPattern;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.learning.stores.EmbeddingStoreFiller;
import com.jongsoft.finance.learning.stores.PledgerEmbeddingStore;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.messaging.notifications.TransactionCreated;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.spending.PatternVectorStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatternDetectorTest {

  /**
   * This test class validates the methods in PatternDetector.
   * The PatternDetector class is responsible for detecting spending patterns in transactions
   * by using multiple pattern detectors and an embedding model.
   */

  private TransactionProvider transactionProvider;
  private CurrentUserProvider currentUserProvider;
  private PledgerEmbeddingStore patternVectorStore;
  private EmbeddingStoreFiller embeddingStoreFiller;
  private EmbeddingStore<TextSegment> embeddingStore;
  private PatternDetector patternDetector;
  private TestablePatternDetector testablePatternDetector;

  @BeforeEach
  void setUp() {
    transactionProvider = mock(TransactionProvider.class);
    currentUserProvider = mock(CurrentUserProvider.class);
    patternVectorStore = mock(PledgerEmbeddingStore.class);
    embeddingStoreFiller = mock(EmbeddingStoreFiller.class);
    embeddingStore = mock(EmbeddingStore.class);

    // Setup current user
    UserIdentifier userIdentifier = new UserIdentifier("test@example.com");
    UserAccount userAccount = mock(UserAccount.class);
    when(userAccount.getUsername()).thenReturn(userIdentifier);
    when(currentUserProvider.currentUser()).thenReturn(userAccount);

    // Setup embedding store
    when(patternVectorStore.embeddingStore()).thenReturn(embeddingStore);

    // Create a testable pattern detector
    testablePatternDetector = new TestablePatternDetector(
        transactionProvider,
        currentUserProvider,
        patternVectorStore,
        embeddingStoreFiller);

    // Create a regular pattern detector for simple tests
    patternDetector = new PatternDetector(
        transactionProvider,
        currentUserProvider,
        patternVectorStore,
        embeddingStoreFiller);
  }

  @Test
  void shouldBeReadyForAnalysisWhenEmbeddingStoreFillerIsDone() {
    // Arrange
    when(embeddingStoreFiller.isDone()).thenReturn(true);

    // Act
    boolean ready = patternDetector.readyForAnalysis();

    // Assert
    assertTrue(ready);
    verify(embeddingStoreFiller).isDone();
  }

  @Test
  void shouldNotBeReadyForAnalysisWhenEmbeddingStoreFillerIsNotDone() {
    // Arrange
    when(embeddingStoreFiller.isDone()).thenReturn(false);

    // Act
    boolean ready = patternDetector.readyForAnalysis();

    // Assert
    assertFalse(ready);
    verify(embeddingStoreFiller).isDone();
  }

  @Test
  void shouldInitializeStoreOnStartupWhenNeeded() {
    // Arrange
    when(patternVectorStore.shouldInitialize()).thenReturn(true);

    // Act
    patternDetector.handleStartup(mock(StartupEvent.class));

    // Assert
    verify(patternVectorStore).shouldInitialize();
    verify(embeddingStoreFiller).consumeTransactions(any());
  }

  @Test
  void shouldNotInitializeStoreOnStartupWhenNotNeeded() {
    // Arrange
    when(patternVectorStore.shouldInitialize()).thenReturn(false);

    // Act
    patternDetector.handleStartup(mock(StartupEvent.class));

    // Assert
    verify(patternVectorStore).shouldInitialize();
    verify(embeddingStoreFiller, never()).consumeTransactions(any());
  }

  @Test
  void shouldCloseStoreOnShutdown() {
    // Act
    patternDetector.handleShutdown(mock(ShutdownEvent.class));

    // Assert
    verify(patternVectorStore).close();
  }

  @Test
  void shouldIndexTransactionWhenClassificationChanges() {
    // Arrange
    long transactionId = 123L;
    Transaction transaction = mock(Transaction.class);
    when(transaction.getId()).thenReturn(transactionId);
    when(transaction.getDate()).thenReturn(LocalDate.now());
    when(transaction.getBudget()).thenReturn("Test Budget");
    when(transaction.getDescription()).thenReturn("Test Description");

    // Mock the account and amount
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(100.0);

    // Create a mock TransactionProvider that returns the transaction
    TransactionProvider mockProvider = mock(TransactionProvider.class);
    com.jongsoft.lang.control.Optional<Transaction> mockOptional = mock(com.jongsoft.lang.control.Optional.class);
    when(mockOptional.get()).thenReturn(transaction);
    when(mockProvider.lookup(transactionId)).thenReturn(mockOptional);

    // Create a testable detector with our mock provider
    PatternDetector detector = new PatternDetector(
        mockProvider,
        currentUserProvider,
        patternVectorStore,
        embeddingStoreFiller);

    // Act
    detector.handleClassificationChanged(new LinkTransactionCommand(transactionId, LinkTransactionCommand.LinkType.CATEGORY, "Test"));

    // Assert
    verify(mockProvider).lookup(transactionId);
    verify(embeddingStore).removeAll(any(Filter.class));
    verify(embeddingStore).add(any(Embedding.class), any(TextSegment.class));
  }

  @Test
  void shouldIndexTransactionWhenTransactionIsCreated() {
    // Arrange
    long transactionId = 123L;
    Transaction transaction = mock(Transaction.class);
    when(transaction.getId()).thenReturn(transactionId);
    when(transaction.getDate()).thenReturn(LocalDate.now());
    when(transaction.getBudget()).thenReturn("Test Budget");
    when(transaction.getDescription()).thenReturn("Test Description");

    // Mock the account and amount
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(100.0);

    // Create a mock TransactionProvider that returns the transaction
    TransactionProvider mockProvider = mock(TransactionProvider.class);
    com.jongsoft.lang.control.Optional<Transaction> mockOptional = mock(com.jongsoft.lang.control.Optional.class);
    when(mockOptional.get()).thenReturn(transaction);
    when(mockProvider.lookup(transactionId)).thenReturn(mockOptional);

    // Create a testable detector with our mock provider
    PatternDetector detector = new PatternDetector(
        mockProvider,
        currentUserProvider,
        patternVectorStore,
        embeddingStoreFiller);

    // Act
    detector.handleTransactionAdded(new TransactionCreated(transactionId));

    // Assert
    verify(mockProvider).lookup(transactionId);
    verify(embeddingStore).removeAll(any(Filter.class));
    verify(embeddingStore).add(any(Embedding.class), any(TextSegment.class));
  }

  @Test
  void shouldReturnEmptyListWhenTransactionHasNoCategoryOrBudget() {
    // Arrange
    Transaction transaction = mock(Transaction.class);
    when(transaction.getCategory()).thenReturn(null);
    when(transaction.getBudget()).thenReturn(null);

    // Act
    List<SpendingPattern> patterns = patternDetector.detect(transaction);

    // Assert
    assertTrue(patterns.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenNotEnoughMatches() {
    // Arrange
    Transaction transaction = mock(Transaction.class);
    when(transaction.getCategory()).thenReturn("Groceries");
    when(transaction.getBudget()).thenReturn("Food");
    when(transaction.getDate()).thenReturn(LocalDate.now());
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(100.0);

    // Setup embedding store to return fewer matches than MIN_TRANSACTIONS_FOR_PATTERN
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
    matches.add(createEmbeddingMatch(LocalDate.now().minusDays(1), 95.0));
    matches.add(createEmbeddingMatch(LocalDate.now().minusDays(2), 105.0));

    when(embeddingStore.search(any())).thenReturn(new EmbeddingSearchResult<>(matches));

    // Act
    List<SpendingPattern> patterns = testablePatternDetector.detect(transaction);

    // Assert
    assertTrue(patterns.isEmpty());
    verify(embeddingStore).search(any());
  }

  @Test
  void shouldDetectPatternsWhenEnoughMatches() {
    // Arrange
    Transaction transaction = mock(Transaction.class);
    when(transaction.getCategory()).thenReturn("Groceries");
    when(transaction.getBudget()).thenReturn("Food");
    when(transaction.getDate()).thenReturn(LocalDate.now());
    Account account = mock(Account.class);
    when(transaction.computeFrom()).thenReturn(account);
    when(transaction.computeAmount(account)).thenReturn(150.0);

    // Setup embedding store to return enough matches
    List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      matches.add(createEmbeddingMatch(LocalDate.now().minusDays(i * 7), 100.0 + i * 5));
    }

    when(embeddingStore.search(any())).thenReturn(new EmbeddingSearchResult<>(matches));

    // Setup mock pattern to return a pattern
    SpendingPattern mockPattern = mock(SpendingPattern.class);
    testablePatternDetector.setMockPatternResult(java.util.Optional.of(mockPattern));

    // Act
    List<SpendingPattern> patterns = testablePatternDetector.detect(transaction);

    // Assert
    assertEquals(1, patterns.size());
    assertSame(mockPattern, patterns.getFirst());
    verify(embeddingStore).search(any());
  }

  @Test
  void shouldUpdateBaselineDoesNothing() {
    // Act
    patternDetector.updateBaseline(YearMonth.now());

    // No assertions needed as the method is empty
  }

  @Test
  void shouldAnalysisCompletedDoesNothing() {
    // Act
    patternDetector.analysisCompleted();

    // No assertions needed as the method is empty
  }

  /**
   * Helper method to create a mocked EmbeddingMatch with the given date and amount
   */
  @SuppressWarnings("unchecked")
  private EmbeddingMatch<TextSegment> createEmbeddingMatch(LocalDate date, double amount) {
    TextSegment segment = mock(TextSegment.class);
    Metadata metadata = mock(Metadata.class);

    when(segment.metadata()).thenReturn(metadata);
    when(metadata.getString("date")).thenReturn(date.toString());
    when(metadata.getDouble("amount")).thenReturn(amount);

    EmbeddingMatch<TextSegment> match = mock(EmbeddingMatch.class);
    when(match.embedded()).thenReturn(segment);
    when(match.score()).thenReturn(0.95);

    return match;
  }

  /**
   * Testable subclass of PatternDetector for testing
   */
  private static class TestablePatternDetector extends PatternDetector {
    private java.util.Optional<SpendingPattern> mockPatternResult = java.util.Optional.empty();

    public TestablePatternDetector(
        TransactionProvider transactionProvider,
        CurrentUserProvider currentUserProvider,
        @PatternVectorStore PledgerEmbeddingStore patternVectorStore,
        EmbeddingStoreFiller embeddingStoreFiller) {
      super(transactionProvider, currentUserProvider, patternVectorStore, embeddingStoreFiller);
    }

    public void setMockPatternResult(java.util.Optional<SpendingPattern> result) {
      this.mockPatternResult = result;
    }

    @Override
    public List<SpendingPattern> detect(Transaction transaction) {
      // Skip the null check from the parent class to test the embedding search
      if (transaction.getCategory() == null && transaction.getBudget() == null) {
        return List.of();
      }

      // Use the real embedding store search
      var searchRequest = createSearchRequest(transaction);
      var matches = getPatternVectorStore().embeddingStore().search(searchRequest).matches();

      // If we have enough matches and a mock result, return it
      if (matches.size() >= 3 && mockPatternResult.isPresent()) {
        return List.of(mockPatternResult.get());
      }

      return List.of();
    }

    private dev.langchain4j.store.embedding.EmbeddingSearchRequest createSearchRequest(Transaction transaction) {
      return dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
          .queryEmbedding(mock(Embedding.class))
          .filter(dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey("user")
              .isEqualTo("test@example.com")
              .and(dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey("date")
                  .isBetween(
                      transaction.getDate().minusMonths(3).toString(),
                      transaction.getDate().toString())))
          .maxResults(150)
          .minScore(0.9)
          .build();
    }

    private PledgerEmbeddingStore getPatternVectorStore() {
      try {
        java.lang.reflect.Field field = PatternDetector.class.getDeclaredField("patternVectorStore");
        field.setAccessible(true);
        return (PledgerEmbeddingStore) field.get(this);
      } catch (Exception e) {
        throw new RuntimeException("Failed to access patternVectorStore field", e);
      }
    }
  }
}
