package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Contract Transaction Resource")
class ContractTransactionResourceTest extends TestSetup {

    @Inject
    private TransactionProvider transactionProvider;

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Test
    @DisplayName("should return transactions for contract")
    void transactions(RequestSpecification spec) {
        Mockito.when(transactionProvider.lookup(Mockito.any())).thenReturn(ResultPage.empty());

        // @formatter:off
        spec
            .when()
                .get("/api/contracts/{contractId}/transactions", 1)
            .then()
                .statusCode(200);
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).contracts(Collections.List(new EntityRef(1L)));
    }
}
