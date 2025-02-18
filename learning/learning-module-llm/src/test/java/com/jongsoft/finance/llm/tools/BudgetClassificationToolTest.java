package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.lang.Control;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetClassificationToolTest {

    @Test
    void listKnownCategories() {
        var mockBudgetProvider = mock(BudgetProvider.class);
        var subject = new BudgetClassificationTool(mockBudgetProvider);

        var budget = Budget.builder().build();
        for (var expense : Arrays.asList("Food", "Transportation", "Shopping", "Entertainment")) {
            budget.new Expense(1, expense.trim(), 100);
        }

        when(mockBudgetProvider.lookup(LocalDate.now().getYear(), LocalDate.now().getMonthValue()))
                .thenReturn(Control.Option(budget));

        var response = subject.listKnownCategories();

        Assertions.assertThat(response)
                .containsExactlyInAnyOrder("Food", "Transportation", "Shopping", "Entertainment");
    }
}