package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryToolTest {

    @Test
    void listKnownSubCategories() {
        var mockProvider = mock(CategoryProvider.class);
        var subject = new CategoryTool(mockProvider);

        when(mockProvider.lookup())
                .thenReturn(Collections.List(
                        Category.builder().label("Groceries").build(),
                        Category.builder().label("Food").build(),
                        Category.builder().label("Car").build()));

        var response = subject.listKnownSubCategories();

        assertThat(response)
                .hasSize(3)
                .containsExactlyInAnyOrder("Groceries", "Food", "Car");
    }
}
