package com.jongsoft.finance.llm;

import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.TransactionResult;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.agent.TransactionExtractorAgent;
import com.jongsoft.finance.llm.dto.AccountDTO;
import com.jongsoft.finance.llm.dto.ClassificationDTO;
import com.jongsoft.finance.llm.dto.TransactionDTO;
import com.jongsoft.finance.llm.stores.ClassificationEmbeddingStore;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSuggestionEngineTest {

    @Test
    void makeSuggestions() {
        // given
        var mockAiAgent = mock(ClassificationAgent.class);
        var subject = new AiSuggestionEngine(mock(ClassificationEmbeddingStore.class), mockAiAgent, mock(TransactionExtractorAgent.class));
        var suggestion = new SuggestionInput(
                LocalDate.of(2022, 1, 1),
                "My transaction",
                "Checking account",
                "Grocery shop",
                22.44);

        when(mockAiAgent.classifyTransaction(any(), any(), any(), anyString(), anyDouble(), anyString()))
                .thenReturn(new ClassificationDTO("Food", "Groceries", List.of("shopping", "groceries")));

        var answer = subject.makeSuggestions(suggestion);

        assertThat(answer)
                .isNotNull()
                .extracting("budget", "category", "tags")
                .containsExactly("Food", "Groceries", List.of("shopping", "groceries"));
    }

    @Test
    void extractTransaction() {
        var mockExtractionAgent = mock(TransactionExtractorAgent.class);
        var subject = new AiSuggestionEngine(mock(ClassificationEmbeddingStore.class), mock(ClassificationAgent.class), mockExtractionAgent);

        when(mockExtractionAgent.extractTransaction(anyString()))
                .thenReturn(new TransactionDTO(
                        new AccountDTO("Checking account", "checking"),
                        new AccountDTO("Savings account", "savings"),
                        "My transaction",
                        LocalDate.of(2010, 1, 1),
                        20.2D,
                        TransactionType.DEBIT));

        var answer = subject.extractTransaction("My transaction");

        assertThat(answer)
                .isPresent()
                .contains(new TransactionResult(
                        TransactionType.DEBIT,
                        LocalDate.of(2010, 1, 1),
                        "Checking account",
                        "Savings account",
                        "My transaction",
                        20.2D));
    }
}
