package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@DisplayName("Transaction resource")
class TransactionResourceTest extends TestSetup {

    @Inject
    private TransactionProvider transactionProvider;
    @Inject
    private AccountProvider accountProvider;
    @Inject
    private AccountTypeProvider accountTypeProvider;

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Replaces
    @MockBean
    AccountTypeProvider accountTypeProvider() {
        return Mockito.mock(AccountTypeProvider.class);
    }

    @Replaces
    @MockBean
    RuntimeResource runtimeResource() {
        return Mockito.mock(RuntimeResource.class);
    }

    @Test
    @DisplayName("should return the search results")
    void search(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();

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
                                                .account(account)
                                                .amount(20.00D)
                                                .build(),
                                        Transaction.Part.builder()
                                                .id(2L)
                                                .account(Account.builder()
                                                        .id(2L)
                                                        .currency("EUR")
                                                        .type("debtor")
                                                        .name("From account")
                                                        .build())
                                                .amount(-20.00D)
                                                .build()
                                ))
                                .build()
                ));

        // @formatter:off
        spec.given()
                .body("""
                        {
                            "page": 0,
                            "dateRange": {
                                "start": "2019-01-01",
                                "end": "2019-02-01"
                            },
                            "onlyIncome": false,
                            "onlyExpense": false,
                            "description": "samp"
                        }""")
            .when()
                .post("/api/transactions")
            .then()
                .statusCode(200)
                .body("info.records", Matchers.equalTo(1))
                .body("content[0].id", Matchers.equalTo(1))
                .body("content[0].description", Matchers.equalTo("Sample transaction"))
                .body("content[0].currency", Matchers.equalTo("EUR"))
                .body("content[0].metadata.category", Matchers.equalTo("Grocery"))
                .body("content[0].metadata.budget", Matchers.equalTo("Household"))
                .body("content[0].dates.transaction", Matchers.equalTo("2019-01-15"));
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).description("samp", false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    @DisplayName("should update part of the transactions")
    void patch(RequestSpecification spec) {
        var transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .currency("EUR")
                .budget("Household")
                .user(ACTIVE_USER)
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
                .build());

        Mockito.when(transactionProvider.lookup(Mockito.anyLong())).thenReturn(Control.Option());
        Mockito.when(transactionProvider.lookup(1L)).thenReturn(Control.Option(transaction));

        // @formatter:off
        spec.given()
                .body("""
                        {
                            "transactions": [1, 2],
                            "budget": {
                                "id": -1,
                                "name": "Groceries"
                            },
                            "category": {
                                "id": -1,
                                "name": "Category"
                            },
                            "contract": {
                                "id": -1,
                                "name": "Wallmart"
                            },
                            "tags": ["sample"]
                        }""")
            .when()
                .patch("/api/transactions")
            .then()
                .statusCode(204);
        // @formatter:on

        Mockito.verify(transaction).linkToCategory("Category");
        Mockito.verify(transaction).linkToBudget("Groceries");
        Mockito.verify(transaction).linkToContract("Wallmart");
        Mockito.verify(transaction).tag(Collections.List("sample"));
    }

    @Test
    @DisplayName("should return the date of the first transaction")
    void firstTransaction(RequestSpecification spec) {
        Mockito.when(transactionProvider.first(Mockito.any())).thenReturn(Control.Option(
                Transaction.builder()
                        .date(LocalDate.of(2019, 1, 1))
                        .build()));

        // @formatter:off
        spec.given()
                .body(Map.of("description", "test"))
            .when()
                .post("/api/transactions/locate-first")
            .then()
                .statusCode(200)
                .body(Matchers.containsString("2019-01-01"));
        // @formatter:on
    }

    @Test
    @DisplayName("should export all known transactions")
    void export(RequestSpecification spec) throws IOException {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();

        Mockito.when(accountTypeProvider.lookup(false)).thenReturn(Collections.List());
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.empty());
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
                                                .account(account)
                                                .amount(20.00D)
                                                .build(),
                                        Transaction.Part.builder()
                                                .id(2L)
                                                .account(Account.builder().id(2L).currency("EUR").type("debtor").name("From account").build())
                                                .amount(-20.00D)
                                                .build()
                                ))
                                .build()
                ))
                .thenReturn(ResultPage.empty());

        // @formatter:off
        spec.when()
                .get("/api/transactions/export")
            .then()
                .statusCode(200)
                .body(Matchers.containsString("Sample transaction"));
        // @formatter:on
    }

}
