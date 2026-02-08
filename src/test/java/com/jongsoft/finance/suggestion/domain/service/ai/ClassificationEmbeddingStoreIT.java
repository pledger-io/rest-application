package com.jongsoft.finance.suggestion.domain.service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionProvider;
import com.jongsoft.finance.banking.domain.model.Transaction;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.budget.adapter.api.BudgetProvider;
import com.jongsoft.finance.budget.domain.model.Budget;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.core.domain.commands.InternalAuthenticationEvent;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.suggestion.domain.model.SuggestionInput;
import com.jongsoft.finance.suggestion.domain.model.SuggestionResult;
import com.jongsoft.lang.Collections;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

class ClassificationEmbeddingStoreIT extends AiBase {

    @Inject
    private AiRuleEngine aiRuleEngine;

    @Inject
    private FilterProvider<TransactionProvider.FilterCommand> filterProvider;

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private AccountProvider accountProvider;

    @Inject
    private CategoryProvider categoryProvider;

    @Test
    void classifyTransaction(PledgerContext pledgerContext) throws InterruptedException {
        pledgerContext
                .withUser("classify-transaction@local")
                .withCategory("Groceries")
                .withCategory("Electronics")
                .withBankAccount("Checking Account", "EUR", "default")
                .withBankAccount("Shopping Account", "EUR", "creditor")
                .withBankAccount("Grocery Account", "EUR", "creditor")
                .withTag("laptop")
                .withTag("groceries")
                .withTag("shopping");

        InternalAuthenticationEvent.authenticate("classify-transaction@local");

        Budget.create(LocalDate.now().minusYears(2), 2500).activate();
        Budget budget = budgetProvider.lookup(LocalDate.now().getYear(), 1).get();
        budget.createExpense("Shopping", 100, 101);

        Transaction.create(
                        accountProvider.lookup("Checking Account").get(),
                        accountProvider.lookup("Shopping Account").get(),
                        LocalDate.now().minusDays(2),
                        1000,
                        "Buy a new laptop")
                .register();
        Transaction.create(
                        accountProvider.lookup("Shopping Account").get(),
                        accountProvider.lookup("Grocery Account").get(),
                        LocalDate.now().minusDays(1),
                        50,
                        "Grocery shopping at the supermarket")
                .register();

        Category electronics = categoryProvider.lookup("Electronics").get();
        Category groceries = categoryProvider.lookup("Groceries").get();
        budget = budgetProvider.lookup(LocalDate.now().getYear(), 1).get();

        Transaction laptopPurchase = transactionProvider
                .lookup(filterProvider.create().description("Buy a new laptop", true))
                .content()
                .get();
        Transaction groceryPurchase = transactionProvider
                .lookup(filterProvider
                        .create()
                        .description("Grocery shopping at the supermarket", true))
                .content()
                .get();

        laptopPurchase.tag(Collections.List("laptop", "shopping"));
        groceryPurchase.tag(Collections.List("groceries", "shopping"));

        laptopPurchase.link(TransactionLinkType.CATEGORY, electronics.getId());
        groceryPurchase.link(TransactionLinkType.CATEGORY, groceries.getId());
        laptopPurchase.link(
                TransactionLinkType.EXPENSE, budget.getExpenses().get(0).getId());
        groceryPurchase.link(
                TransactionLinkType.EXPENSE, budget.getExpenses().get(0).getId());

        Thread.sleep(50);

        var suggestion = aiRuleEngine.makeSuggestions(
                new SuggestionInput(null, "Shopping for a laptop", null, null, 0));
        assertThat(suggestion)
                .as("Suggestion should be present for laptop")
                .isEqualTo(new SuggestionResult(
                        "Shopping", "Electronics", List.of("laptop", "shopping")));

        var grocerySuggestion = aiRuleEngine.makeSuggestions(
                new SuggestionInput(null, "Weekly grocery shopping", null, null, 0));
        assertThat(grocerySuggestion)
                .as("Suggestion should be present for groceries")
                .isEqualTo(new SuggestionResult(
                        "Shopping", "Groceries", List.of("groceries", "shopping")));
    }
}
