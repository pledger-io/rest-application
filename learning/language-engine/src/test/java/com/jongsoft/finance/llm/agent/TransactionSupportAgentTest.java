package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

@Disabled("Only ran locally to test this new module. Not production ready yet.")
@MicronautTest(environments = "ai")
class TransactionSupportAgentTest {

    @Inject
    private TransactionSupportAgent transactionSupportAgent;

    @Inject
    private CategoryProvider categoryProvider;

    @Inject
    private BudgetProvider budgetProvider;

    @Inject
    private TagProvider tagProvider;

    @MockBean
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @MockBean
    CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @MockBean
    TagProvider tagProvider() {
        return Mockito.mock(TagProvider.class);
    }

    @Test
    void verifyClassificationOfTransactions() {
        Mockito.when(categoryProvider.lookup()).thenReturn(Collections.List(
                Category.builder().label("Groceries").build(),
                Category.builder().label("Online shopping").build(),
                Category.builder().label("Dentist expenses").build(),
                Category.builder().label("Health Insurance").build(),
                Category.builder().label("Rent or mortgage").build()
        ));
        var budget = Budget.builder().build();
        budget.new Expense(1L, "Shopping", 100);
        budget.new Expense(2L, "Health", 100);
        budget.new Expense(3L, "Fixed expenses", 100);
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Control.Option(budget));
        Mockito.when(tagProvider.lookup()).thenReturn(Collections.List(
                new Tag("Vacation"),
                new Tag("Insurance"),
                new Tag("Healthcare"),
                new Tag("Streaming Services"),
                new Tag("TV")
        ));

        LoggerFactory.getLogger(getClass())
                .atInfo()
                .addArgument("Breezy Point Smile Appointment")
                .addArgument(transactionSupportAgent.classify(1, "2024-01-12 Breezy Point Smile Appointment").text())
                .log("Question:\n {}\nAnswer:\n{}");

        LoggerFactory.getLogger(getClass())
                .atInfo()
                .addArgument("Netflix Monthly Fee")
                .addArgument(transactionSupportAgent.classify(2, "2025-02-15 Netflix Monthly Fee").text())
                .log("Question:\n {}\nAnswer:\n{}");

        LoggerFactory.getLogger(getClass())
                .atInfo()
                .addArgument("Day at Fern River Resorts 2022")
                .addArgument(transactionSupportAgent.classify(3,"Day at Fern River Resorts 2022").text())
                .log("Question:\n {}\nAnswer:\n{}");

        LoggerFactory.getLogger(getClass())
                .info(transactionSupportAgent.classify(4,"Azure bill").text());

        LoggerFactory.getLogger(getClass())
                .atInfo()
                .addArgument("Weekly grocery shopping")
                .addArgument(transactionSupportAgent.classify(5,"Weekly grocery shopping").text())
                .log("Question:\n {}\nAnswer:\n{}");
    }
}