package com.jongsoft.finance.llm.feature;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.learning.SuggestionInput;
import com.jongsoft.finance.learning.SuggestionResult;
import com.jongsoft.finance.llm.stores.ClassificationEmbeddingStore;
import com.jongsoft.finance.messaging.commands.transaction.LinkTransactionCommand;
import com.jongsoft.finance.providers.ExpenseProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ClassificationEmbeddingStoreIT extends AiBase {

    @Inject
    private ClassificationEmbeddingStore classificationEmbeddingStore;

    @Inject private TransactionProvider transactionProvider;
    @Inject private ExpenseProvider expenseProvider;

    @Test
    void classifyTransaction() throws InterruptedException {
        when(expenseProvider.lookup(1L)).thenReturn(Control.Option(new EntityRef.NamedEntity(1L, "Shopping")));

        var transaction = Transaction.builder()
                .id(1L)
                .description("Buy a new laptop")
                .metadata(Map.of(
                    "CATEGORY", Category.builder().label("Electronics").build(),
                    "EXPENSE", new EntityRef.NamedEntity(1L, "Shopping")))
                .tags(Collections.List("laptop", "shopping"))
                .build();
        var transaction2 = Transaction.builder()
                .id(2L)
                .description("Grocery shopping at the supermarket")
                .metadata(Map.of(
                    "CATEGORY", Category.builder().label("Groceries").build(),
                    "EXPENSE", new EntityRef.NamedEntity(1L, "Shopping")))
                .tags(Collections.List("groceries", "shopping"))
                .build();

        when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));
        when(transactionProvider.lookup(2L)).thenReturn(Control.Option(transaction2));
        transaction.link(LinkTransactionCommand.LinkType.EXPENSE, 1L);
        transaction2.link(LinkTransactionCommand.LinkType.EXPENSE, 1L);

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
                .metadata(
                    Map.of(
                        "CATEGORY", Category.builder().label("Electronics").build(),
                        "EXPENSE", new EntityRef.NamedEntity(1L, "Shopping")))
                .tags(Collections.List("laptop", "shopping"))
                .build();
        var updatedTransaction = Transaction.builder()
            .id(1L)
            .description("Buy a new laptop")
            .metadata(
                Map.of(
                    "CATEGORY", Category.builder().label("Online shopping").build(),
                    "EXPENSE", new EntityRef.NamedEntity(1L, "Shopping")))
            .tags(Collections.List("laptop", "shopping"))
            .build();
        when(transactionProvider.lookup(1L))
            .thenReturn(Control.Option(transaction))
            .thenReturn(Control.Option(updatedTransaction));

        transaction.link(LinkTransactionCommand.LinkType.EXPENSE, 1L);
        var suggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Suggestion should be present for laptop")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Electronics", List.of("laptop", "shopping")));

        transaction.link(LinkTransactionCommand.LinkType.CATEGORY, 2L);
        Thread.sleep(50);
        suggestion = classificationEmbeddingStore.classify(new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Updated suggestion should be present for laptop")
                .isPresent()
                .contains(new SuggestionResult("Shopping", "Online shopping", List.of("laptop", "shopping")));
    }
}
