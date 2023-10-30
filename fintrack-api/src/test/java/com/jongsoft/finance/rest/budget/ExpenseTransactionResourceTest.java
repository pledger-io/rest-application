package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Expenses for budget resource")
class ExpenseTransactionResourceTest extends TestSetup {

    @Inject
    private TransactionProvider transactionProvider;

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Test
    @DisplayName("Fetch transactions for expense")
    void transactions(RequestSpecification spec) {
        Mockito.when(transactionProvider.lookup(Mockito.any())).thenReturn(ResultPage.empty());

        // @formatter:off
        spec
            .when()
                .get("/api/budgets/expenses/{expenseId}/{year}/{month}/transactions", 1L, 2016, 1)
            .then()
                .statusCode(200);
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).expenses(Collections.List(new EntityRef(1L)));
    }
}