package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TagClassificationToolTest {

    @Test
    void listKnownTags() {
        var mockProvider = mock(TagProvider.class);
        var subject = new TagTool(mockProvider);

        when(mockProvider.lookup()).thenReturn(Collections.List(
                new Tag("food"),
                new Tag("beverage")));

        var response = subject.listKnownTags();

        assertThat(response)
                .hasSize(2)
                .containsExactlyInAnyOrder("food", "beverage");
    }
}
