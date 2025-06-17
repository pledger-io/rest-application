package com.jongsoft.finance.spending.scheduler;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.insight.Insight;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.spending.Detector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisRunnerTest {
    private List<Detector<?>> transactionDetectors;
    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;
    private AnalysisRunner analysisRunner;
    private Detector<?> mockDetector;
    private TransactionProvider.FilterCommand filterCommand;

    @BeforeEach
    void setUp() {
        // Create mocks
        mockDetector = mock(Detector.class);
        transactionDetectors = List.of(mockDetector);
        filterFactory = mock(FilterFactory.class);
        transactionProvider = mock(TransactionProvider.class);
        filterCommand = mock(TransactionProvider.FilterCommand.class);

        // Create filter factory chain mocks
        when(filterFactory.transaction()).thenReturn(filterCommand);
        when(filterCommand.range(any())).thenReturn(filterCommand);

        // Create the analysis runner
        analysisRunner = new AnalysisRunner(transactionDetectors, filterFactory, transactionProvider);
    }

    @Test
    void shouldReturnFalseWhenNotAllDetectorsAreReady() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(false);
        YearMonth month = YearMonth.now();

        // Act
        boolean result = analysisRunner.analyzeForUser(month);

        // Assert
        assertFalse(result);
        verify(mockDetector).readyForAnalysis();
        verify(transactionProvider, never()).lookup(any());
    }

    @Test
    void shouldReturnFalseWhenNoTransactionsForMonth() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);
        YearMonth month = YearMonth.now();

        ResultPage<Transaction> emptyPage = ResultPage.empty();
        when(transactionProvider.lookup(any())).thenReturn(emptyPage);

        // Act
        boolean result = analysisRunner.analyzeForUser(month);

        // Assert
        assertFalse(result);
        verify(mockDetector).readyForAnalysis();
        verify(transactionProvider).lookup(any());
        verify(mockDetector, never()).updateBaseline(any());
    }

    @Test
    void shouldReturnTrueWhenTransactionsExistAndDetectorsAreReady() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);
        YearMonth month = YearMonth.now();

        // Create a mock transaction
        Transaction mockTransaction = mock(Transaction.class);
        when(mockTransaction.getCategory()).thenReturn("TestCategory");

        // Create a mock insight
        Insight mockInsight = mock(Insight.class);
        List<Insight> insights = new ArrayList<>();
        insights.add(mockInsight);
        doReturn(insights).when(mockDetector).detect(any(Transaction.class));

        // Set up transaction provider to return our mock transaction
        ResultPage<Transaction> transactionPage = ResultPage.of(mockTransaction);
        when(transactionProvider.lookup(any())).thenReturn(transactionPage);

        // Act
        boolean result = analysisRunner.analyzeForUser(month);

        // Assert
        assertTrue(result);
        verify(mockDetector).readyForAnalysis();
        verify(transactionProvider).lookup(any());
        verify(mockDetector).updateBaseline(month);
        verify(mockDetector).detect(mockTransaction);
        verify(mockInsight).signal();
    }

    @Test
    void shouldReturnFalseWhenExceptionOccurs() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);
        YearMonth month = YearMonth.now();

        // Make the transaction provider throw an exception
        when(transactionProvider.lookup(any())).thenThrow(new RuntimeException("Test exception"));

        // Act
        boolean result = analysisRunner.analyzeForUser(month);

        // Assert
        assertFalse(result);
        verify(mockDetector).readyForAnalysis();
        verify(transactionProvider).lookup(any());
    }

    @Test
    void shouldProcessTransactionWithCategory() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);

        // Create a mock transaction with a category
        Transaction mockTransaction = mock(Transaction.class);
        when(mockTransaction.getCategory()).thenReturn("TestCategory");
        when(mockTransaction.getBudget()).thenReturn(null);

        // Create a mock insight
        Insight mockInsight = mock(Insight.class);
        List<Insight> insights = new ArrayList<>();
        insights.add(mockInsight);
        doReturn(insights).when(mockDetector).detect(any(Transaction.class));

        // Set up transaction provider
        ResultPage<Transaction> transactionPage = ResultPage.of(mockTransaction);
        when(transactionProvider.lookup(any())).thenReturn(transactionPage);

        // Act
        boolean result = analysisRunner.analyzeForUser(YearMonth.now());

        // Assert
        assertTrue(result);
        verify(mockDetector).detect(mockTransaction);
        verify(mockInsight).signal();
    }

    @Test
    void shouldProcessTransactionWithBudget() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);

        // Create a mock transaction with a budget
        Transaction mockTransaction = mock(Transaction.class);
        when(mockTransaction.getCategory()).thenReturn(null);
        when(mockTransaction.getBudget()).thenReturn("TestBudget");

        // Create a mock insight
        Insight mockInsight = mock(Insight.class);
        List<Insight> insights = new ArrayList<>();
        insights.add(mockInsight);
        doReturn(insights).when(mockDetector).detect(any(Transaction.class));

        // Set up transaction provider
        ResultPage<Transaction> transactionPage = ResultPage.of(mockTransaction);
        when(transactionProvider.lookup(any())).thenReturn(transactionPage);

        // Act
        boolean result = analysisRunner.analyzeForUser(YearMonth.now());

        // Assert
        assertTrue(result);
        verify(mockDetector).detect(mockTransaction);
        verify(mockInsight).signal();
    }

    @Test
    void shouldSkipTransactionWithoutCategoryOrBudget() {
        // Arrange
        when(mockDetector.readyForAnalysis()).thenReturn(true);

        // Create a mock transaction without category or budget
        Transaction mockTransaction = mock(Transaction.class);
        when(mockTransaction.getCategory()).thenReturn(null);
        when(mockTransaction.getBudget()).thenReturn(null);

        // Set up transaction provider
        ResultPage<Transaction> transactionPage = ResultPage.of(mockTransaction);
        when(transactionProvider.lookup(any())).thenReturn(transactionPage);

        // Act
        boolean result = analysisRunner.analyzeForUser(YearMonth.now());

        // Assert
        assertTrue(result);
        verify(mockDetector, never()).detect(mockTransaction);
    }
}
