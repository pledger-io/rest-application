package com.jongsoft.finance.rule;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.learning.SuggestionInput;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class RuleSuggestionEngineTest {

    @Test
    void makeSuggestions() {
        var mockRuleEngine = mock(RuleEngine.class);
        var suggestionEngine = new RuleSuggestionEngine(mockRuleEngine);
        var ruleOutput = new RuleDataSet();

        when(mockRuleEngine.run(any())).thenReturn(ruleOutput);

        var inputTransaction = new SuggestionInput(
                LocalDate.of(2010,11,2),
                "My grocery shopping",
                "Checking account",
                "Grocery store",
                        12.22);
        suggestionEngine.makeSuggestions(inputTransaction);

        var datesetCaptor = ArgumentCaptor.forClass(RuleDataSet.class);
        verify(mockRuleEngine).run(datesetCaptor.capture());
        Assertions.assertThat(datesetCaptor.getValue())
                .isNotNull()
                .hasSize(4)
                .containsEntry(RuleColumn.AMOUNT, 12.22)
                .containsEntry(RuleColumn.DESCRIPTION, "My grocery shopping")
                .containsEntry(RuleColumn.SOURCE_ACCOUNT, "Checking account")
                .containsEntry(RuleColumn.TO_ACCOUNT, "Grocery store");
    }
}