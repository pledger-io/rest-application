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
        var budget = List.of("Travel Expenses", "Fixed expenses", "Insurances", "Entertainment", "Health Expenses");
        var categories = List.of("Mortgage payments", "Savings", "Investments", "Groceries", "Healthcare", "Online shopping");
        var tags = List.of("Health", "Phone", "Streaming service", "Insurances", "Vacation 2022", "Vacation 2023", "Music");

        LoggerFactory.getLogger(getClass())
                .info(transactionSupportAgent.classify(budget, categories, tags, "Breezy Point Smile Appointment").text());

        LoggerFactory.getLogger(getClass())
                .info(transactionSupportAgent.classify(budget, categories, tags, "Netflix Monthly").text());

        LoggerFactory.getLogger(getClass())
                .info(transactionSupportAgent.classify(budget, categories, tags, "Day at Fern River Resorts 2022").text());

        LoggerFactory.getLogger(getClass())
                .info(transactionSupportAgent.classify(budget, categories, tags, "Azure bill").text());
    }
}