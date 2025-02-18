package com.jongsoft.finance.llm.augmenters;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClassificationAugmenterTest {

    private final String messageBase = "Pick the correct %s for a transaction on ";

    private BudgetProvider budgetProvider;
    private CategoryProvider categoryProvider;
    private TagProvider tagProvider;
    private ClassificationAugmenter augmenter;

    @BeforeEach
    void setup() {
        budgetProvider = mock(BudgetProvider.class);
        categoryProvider = mock(CategoryProvider.class);
        tagProvider = mock(TagProvider.class);
        augmenter = new ClassificationAugmenter(budgetProvider, categoryProvider, tagProvider);
    }

    @Test
    void augmentForBudget() {
        var budget = Budget.builder().build();
        for (var expense : Arrays.asList("Food", "Transportation", "Shopping", "Entertainment")) {
            budget.new Expense(1, expense.trim(), 100);
        }
        when(budgetProvider.lookup(LocalDate.now().getYear(), LocalDate.now().getMonthValue()))
                .thenReturn(Control.Option(budget));

        var response = augmenter.augment(UserMessage.userMessage(messageBase.formatted("category")), null);

        assertThat(response.singleText())
                .isNotBlank()
                .contains("You must choose from the following options: [Food,Transportation,Shopping,Entertainment]");
    }

    @Test
    void augmentForCategory() {
        when(categoryProvider.lookup())
                .thenReturn(Collections.List(
                        Category.builder().label("Groceries").build(),
                        Category.builder().label("Food").build(),
                        Category.builder().label("Car").build()));

        var response = augmenter.augment(UserMessage.userMessage(messageBase.formatted("subcategory")), null);

        assertThat(response.singleText())
                .isNotBlank()
                .contains("You must choose from the following options: [Groceries,Food,Car]");
    }

    @Test
    void augmentForTags() {
        when(tagProvider.lookup()).thenReturn(Collections.List(
                new com.jongsoft.finance.domain.transaction.Tag("food"),
                new Tag("beverage")));

        var response = augmenter.augment(UserMessage.userMessage(messageBase.formatted("tags")), null);

        assertThat(response.singleText())
                .isNotBlank()
                .contains("You must choose from the following options: [food,beverage]");
    }
}