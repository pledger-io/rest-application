package com.jongsoft.finance.llm.agent;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.LoggerFactory;

import java.util.List;

@Disabled("Only ran locally to test this new module. Not production ready yet.")
@MicronautTest(environments = "ai")
class TransactionSupportAgentTest {

    @Inject
    private TransactionSupportAgent transactionSupportAgent;
    @Inject
    private BudgetProvider budgetProvider;

    @MockBean
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @MockBean
    FilterFactory filterFactory() {
        var filterFactory = Mockito.mock(FilterFactory.class);
        Mockito.when(filterFactory.transaction()).thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.account()).thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, InvocationOnMock::getMock));
        return filterFactory;
    }

    @Test
    void askForSupport() {
        var budget = List.of("Travel Expenses", "Fixed expenses", "Insurances", "Entertainment");
        var categories = List.of("Mortgage payments", "Savings", "Investments", "Groceries", "Healthcare");
        var tags = List.of("Spending", "Health", "Phone", "Streaming service", "Insurances", "Vacation 2022", "Vacation 2023");

        var response = transactionSupportAgent.classify(budget, categories, tags, "Monthly health insurance payment");
        LoggerFactory.getLogger(getClass()).info(response.text());

        response = transactionSupportAgent.classify(budget, categories, tags, "Monthly payment to Netflix");
        LoggerFactory.getLogger(getClass()).info(response.text());

        response = transactionSupportAgent.classify(budget, categories, tags, "Hotel costs 2022");
        LoggerFactory.getLogger(getClass()).info(response.text());
    }
}