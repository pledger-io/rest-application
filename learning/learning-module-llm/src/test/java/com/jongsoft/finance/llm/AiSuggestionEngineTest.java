package com.jongsoft.finance.llm;

import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.llm.agent.ClassificationAgent;
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
        var subject = new AiSuggestionEngine(mockAiAgent);
        var suggestion = new SuggestionInput(
                LocalDate.of(2022, 1, 1),
                "My transaction",
                "Checking account",
                "Grocery shop",
                22.44);

        when(mockAiAgent.determineCategory(any(), any(), any(), anyString(), anyDouble(), anyString()))
                .thenReturn("Food");
        when(mockAiAgent.determineSubCategory(any(), any(), any(), anyString(), anyDouble(), anyString()))
                .thenReturn("Groceries");
        when(mockAiAgent.determineTags(any(), any(), any(), anyString(), anyDouble(), anyString()))
                .thenReturn(List.of("shopping", "groceries"));

        var answer = subject.makeSuggestions(suggestion);

        assertThat(answer)
                .isNotNull()
                .extracting("budget", "category", "tags")
                .containsExactly("Food", "Groceries", List.of("shopping", "groceries"));
    }
}