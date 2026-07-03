package com.jongsoft.finance.spending.domain.service.detector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.commands.LinkTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.TransactionCreated;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.banking.domain.model.EntityRef;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.configuration.SpendingAnalysisConfiguration;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.domain.service.vector.PledgerVectorStore;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;

import io.micronaut.context.event.StartupEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Tag("unit")
@DisplayName("Unit - Pattern Detector")
class PatternDetectorTest {

    private TransactionProvider transactionProvider;
    private FilterProvider<TransactionProvider.FilterCommand> filterProvider;
    private CurrentUserProvider currentUserProvider;
    private UserProvider userProvider;
    private PledgerVectorStore patternVectorStore;
    private EmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;
    private PatternDetector patternDetector;

    private Map<String, ? extends Classifier> forExpense(String expense) {
        return Map.of("EXPENSE", new EntityRef.NamedEntity(1L, expense));
    }

    @BeforeEach
    void setUp() {
        transactionProvider = mock(TransactionProvider.class);
        filterProvider = mock(FilterProvider.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        userProvider = mock(UserProvider.class);
        patternVectorStore = mock(PledgerVectorStore.class);
        embeddingStore = mock(EmbeddingStore.class);
        embeddingModel = mock(EmbeddingModel.class);

        when(patternVectorStore.embeddingStore()).thenReturn(embeddingStore);
        when(userProvider.lookup()).thenReturn(Collections.List());
        when(embeddingModel.embed(any(TextSegment.class)))
                .thenReturn(Response.from(Embedding.from(new float[] {0.1f, 0.2f})));

        patternDetector = new PatternDetector(
                transactionProvider,
                filterProvider,
                currentUserProvider,
                userProvider,
                new SpendingAnalysisConfiguration(),
                embeddingModel,
                patternVectorStore);
    }

    @Test
    @DisplayName("Should not be ready before startup completes")
    void shouldNotBeReadyBeforeStartup() {
        assertFalse(patternDetector.readyForAnalysis());
    }

    @Test
    @DisplayName("Should become ready after startup when vector store initialization is skipped")
    void shouldBeReadyWhenInitializationSkipped() {
        when(patternVectorStore.shouldInitialize()).thenReturn(false);

        patternDetector.handleStartup(mock(StartupEvent.class));

        assertTrue(patternDetector.readyForAnalysis());
    }

    @Test
    @DisplayName("Should become ready after startup when vector indexing completes")
    void shouldBeReadyAfterIndexingCompletes() {
        when(patternVectorStore.shouldInitialize()).thenReturn(true);

        patternDetector.handleStartup(mock(StartupEvent.class));

        assertTrue(patternDetector.readyForAnalysis());
    }

    @Test
    @DisplayName("Should return empty for transaction-level detect")
    void shouldReturnEmptyForTransactionDetect() {
        Transaction transaction = mock(Transaction.class);
        assertTrue(patternDetector.detect(transaction).isEmpty());
    }

    @Test
    @DisplayName("Should return empty for month detect when no transactions")
    void shouldReturnEmptyForEmptyMonth() {
        assertTrue(
                patternDetector.detectForMonth(YearMonth.of(2025, 5), List.of()).isEmpty());
    }

    @Test
    @DisplayName("Should skip indexing when transaction lookup is empty on classification change")
    void shouldSkipIndexingWhenTransactionNotFoundOnClassificationChange() {
        when(transactionProvider.lookup(42L)).thenReturn(Control.Option());

        patternDetector.handleClassificationChanged(
                new LinkTransactionCommand(42L, TransactionLinkType.EXPENSE, 1L));

        verify(embeddingStore, never()).add(any(Embedding.class), any(TextSegment.class));
    }

    @Test
    @DisplayName("Should skip indexing when transaction lookup is empty on transaction added")
    void shouldSkipIndexingWhenTransactionNotFoundOnTransactionAdded() {
        when(transactionProvider.lookup(99L)).thenReturn(Control.Option());

        patternDetector.handleTransactionAdded(new TransactionCreated(99L));

        verify(embeddingStore, never()).add(any(Embedding.class), any(TextSegment.class));
    }

    @Test
    @DisplayName("Should index transaction when classification changes and transaction exists")
    void shouldIndexTransactionOnClassificationChange() {
        UserAccount user = mock(UserAccount.class);
        when(user.getUsername()).thenReturn(new UserIdentifier("user@test.com"));
        when(currentUserProvider.currentUser()).thenReturn(user);

        Transaction transaction = mock(Transaction.class);
        doReturn(forExpense("Groceries")).when(transaction).getMetadata();
        when(transaction.getId()).thenReturn(1L);
        when(transaction.getDate()).thenReturn(YearMonth.now().atDay(1));
        when(transaction.getDescription()).thenReturn("Weekly shop");
        when(transaction.computeTo()).thenReturn(null);
        when(transaction.computeAmount(null)).thenReturn(50.0);
        when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));

        patternDetector.handleClassificationChanged(
                new LinkTransactionCommand(1L, TransactionLinkType.EXPENSE, 1L));

        verify(embeddingStore).add(any(Embedding.class), any(TextSegment.class));
    }
}
