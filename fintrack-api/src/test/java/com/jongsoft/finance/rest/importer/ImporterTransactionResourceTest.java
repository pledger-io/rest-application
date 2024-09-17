package com.jongsoft.finance.rest.importer;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.process.RuntimeResource;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

@DisplayName("Import transactions resource")
class ImporterTransactionResourceTest extends TestSetup {

    @Inject
    private TransactionProvider transactionProvider;

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Replaces
    @MockBean
    RuntimeResource runtimeResource() {
        return Mockito.mock(RuntimeResource.class);
    }

    @Test
    @DisplayName("Search transactions by batch slug")
    void search(RequestSpecification spec) {
        Mockito.when(transactionProvider.lookup(Mockito.any()))
                .thenReturn(ResultPage.of(
                        Transaction.builder()
                                .id(1L)
                                .description("Sample transaction")
                                .category("Grocery")
                                .currency("EUR")
                                .budget("Household")
                                .date(LocalDate.of(2019, 1, 15))
                                .transactions(Collections.List(
                                        Transaction.Part.builder()
                                                .id(1L)
                                                .account(Account.builder()
                                                        .id(1L)
                                                        .name("To account")
                                                        .type("checking")
                                                        .currency("EUR")
                                                        .build())
                                                .amount(20.00D)
                                                .build(),
                                        Transaction.Part.builder()
                                                .id(2L)
                                                .account(Account.builder().id(2L).currency("EUR").type("debtor").name("From account").build())
                                                .amount(-20.00D)
                                                .build()
                                ))
                                .build()));

        // @formatter:off
        spec
            .given()
                .body(new TransactionSearchRequest(0))
            .when()
                .post("/api/import/{batchSlug}/transactions", "ads-fasdfa-fasd")
            .then()
                .statusCode(200)
                .body("content[0].description", Matchers.equalTo("Sample transaction"));
        // @formatter:on

        var mockFilter = filterFactory.transaction();

        Mockito.verify(mockFilter).importSlug("ads-fasdfa-fasd");
        Mockito.verify(mockFilter).page(0);
        Mockito.verify(transactionProvider).lookup(Mockito.any());
    }

    @Test
    @DisplayName("Delete transaction attached to batch job")
    void delete(RequestSpecification spec) {
        Transaction transaction = Mockito.mock(Transaction.class);

        Mockito.when(transaction.getUser()).thenReturn(ACTIVE_USER);
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        // @formatter:off
        spec
            .when()
                .delete("/api/import/{batchSlug}/transactions/{transactionId}", "ads-fasdfa-fasd", 123L)
            .then()
                .statusCode(204);
        // @formatter:on

        Mockito.verify(transactionProvider).lookup(123L);
        Mockito.verify(transaction).delete();
    }

}
