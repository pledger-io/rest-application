package com.jongsoft.finance.llm.feature;

import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.llm.stores.ClassificationEmbeddingStore;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationEmbeddingStoreIT extends AiBase {

    @Inject
    private ClassificationEmbeddingStore classificationEmbeddingStore;

    @Inject private TransactionProvider transactionProvider;

    @Test
    void classifyTransaction() throws InterruptedException {
        var transaction = Transaction.builder()
                .id(1L)
                .description("Buy a new laptop")
                .category("Electronics")
                .tags(Collections.List("laptop", "shopping"))
                .build();
        var transaction2 = Transaction.builder()
                .id(2L)
                .description("Grocery shopping at the supermarket")
                .category("Groceries")
                .tags(Collections.List("groceries", "shopping"))
                .build();

        Mockito.when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));
        Mockito.when(transactionProvider.lookup(2L)).thenReturn(Control.Option(transaction2));
        transaction.linkToBudget("Shopping");
        transaction2.linkToBudget("Shopping");

        Thread.sleep(50);

        var suggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Suggestion should be present for laptop")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Electronics", List.of("laptop", "shopping")));

        var grocerySuggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Weekly grocery shopping", null, null, 0));
        assertThat(grocerySuggestion)
                .as("Suggestion should be present for groceries")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Groceries", List.of("groceries", "shopping")));
    }

    @Test
    void ensureUpdatingCorrectly() throws InterruptedException {
        var transaction = Transaction.builder()
                .id(1L)
                .description("Buy a new laptop")
                .category("Electronics")
                .tags(Collections.List("laptop", "shopping"))
                .build();
        Mockito.when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));

        transaction.linkToBudget("Shopping");
        var suggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Suggestion should be present for laptop")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Electronics", List.of("laptop", "shopping")));

        transaction.linkToCategory("Online shopping");
        Thread.sleep(50);
        suggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Updated suggestion should be present for laptop")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Online shopping", List.of("laptop", "shopping")));
    }
}
