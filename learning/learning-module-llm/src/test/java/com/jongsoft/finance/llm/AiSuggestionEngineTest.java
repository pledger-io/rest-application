package com.jongsoft.finance.llm;

import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
import com.jongsoft.finance.llm.dto.ClassificationDTO;
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
        var subject = new AiSuggestionEngine(mock(ClassificationEmbeddingStore.class), mockAiAgent);
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
}
