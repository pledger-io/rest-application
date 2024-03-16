package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

@DisplayName("Account transaction resource")
class AccountTransactionResourceTest extends TestSetup {

    private AccountTransactionResource subject;

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private TransactionProvider transactionProvider;

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Replaces
    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Test
    @DisplayName("Search for transactions")
    void search(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

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

        spec.when()
                .body("""
                        {
                            "dateRange": {
                                "from": "2019-01-01",
                                "until": "2019-12-31"
                            }
                        }""")
                .post("/api/accounts/{accountId}/transactions", 1)
            .then()
                .statusCode(200)
                .body("info.records", Matchers.equalTo(1))
                .body("content[0].id", Matchers.equalTo(1))
                .body("content[0].description", Matchers.equalTo("Sample transaction"))
                .body("content[0].currency", Matchers.equalTo("EUR"))
                .body("content[0].type.code", Matchers.equalTo("DEBIT"))
                .body("content[0].metadata.category", Matchers.equalTo("Grocery"))
                .body("content[0].metadata.budget", Matchers.equalTo("Household"))
                .body("content[0].dates.transaction", Matchers.equalTo("2019-01-15"))
                .body("content[0].destination.id", Matchers.equalTo(1))
                .body("content[0].source.id", Matchers.equalTo(2))
                .body("content[0].amount", Matchers.equalTo(20.0F));

        // @formatter:on

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(transactionProvider).lookup(Mockito.any());
    }

    @Test
    @DisplayName("Search for transactions with missing account")
    void search_notfound(RequestSpecification spec) {
        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option());

        // @formatter:off
        spec.when()
                .body("""
                        {
                            "dateRange": {
                                "from": "2019-01-01",
                                "until": "2019-12-31"
                            }
                        }""")
                .post("/api/accounts/{accountId}/transactions", 1)
            .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("Account not found with id 1"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a transaction for account")
    void create(RequestSpecification spec) {
        final Account myAccount = Mockito.spy(Account.builder().id(1L).currency("EUR").type("checking").name("My account").build());
        final Account toAccount = Account.builder().id(2L).currency("EUR").type("creditor").name("Target account").build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(myAccount));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(toAccount));

        // @formatter:off

        spec.when()
                .body("""
                        {
                            "date": "2019-01-01",
                            "currency": "EUR",
                            "description": "Sample transaction",
                            "amount": 20.2,
                            "source": {
                                "id": 1
                            },
                            "destination": {
                                "id": 2
                            },
                            "category": {
                                "id": 1
                            },
                            "budget": {
                                "id": 3
                            }
                        }""")
                .put("/api/accounts/{accountId}/transactions", 1)
            .then()
                .statusCode(204);

        // @formatter:on

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(accountProvider).lookup(2L);
        Mockito.verify(myAccount).createTransaction(
                Mockito.eq(toAccount),
                Mockito.eq(20.2D),
                Mockito.eq(Transaction.Type.CREDIT),
                Mockito.any());
    }

    @Test
    @DisplayName("Fetch the first transaction for account")
    void first(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
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
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.first(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(Control.Option(transaction));

        // @formatter:off

        spec.when()
                .get("/api/accounts/{accountId}/transactions/first", 1)
            .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(1))
                .body("description", Matchers.equalTo("Sample transaction"))
                .body("currency", Matchers.equalTo("EUR"))
                .body("type.code", Matchers.equalTo("DEBIT"))
                .body("metadata.category", Matchers.equalTo("Grocery"))
                .body("metadata.budget", Matchers.equalTo("Household"))
                .body("dates.transaction", Matchers.equalTo("2019-01-15"))
                .body("destination.id", Matchers.equalTo(1))
                .body("source.id", Matchers.equalTo(2))
                .body("amount", Matchers.equalTo(20.0F));

        // @formatter:on
    }

    @Test
    @DisplayName("Fetch transaction by id")
    void get(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
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
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        // @formatter:off

        spec.when()
                .get("/api/accounts/{accountId}/transactions/{transactionId}", 1, 123)
            .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(1))
                .body("description", Matchers.equalTo("Sample transaction"))
                .body("currency", Matchers.equalTo("EUR"))
                .body("type.code", Matchers.equalTo("DEBIT"))
                .body("metadata.category", Matchers.equalTo("Grocery"))
                .body("metadata.budget", Matchers.equalTo("Household"))
                .body("dates.transaction", Matchers.equalTo("2019-01-15"))
                .body("destination.id", Matchers.equalTo(1))
                .body("source.id", Matchers.equalTo(2))
                .body("amount", Matchers.equalTo(20.0F));

        // @formatter:on
    }

    @Test
    @DisplayName("Update transaction by id")
    void update(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();
        final Account toAccount = Account.builder()
                .id(2L)
                .currency("EUR")
                .type("debtor")
                .name("From account").build();

        Transaction transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .currency("EUR")
                .budget("Household")
                .date(LocalDate.of(2019, 1, 15))
                .user(ACTIVE_USER)
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .account(account)
                                .amount(20.00D)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .account(toAccount)
                                .amount(-20.00D)
                                .build()
                ))
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(toAccount));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        // @formatter:off

        spec.when()
                .body("""
                        {
                            "date": "2019-01-01",
                            "currency": "EUR",
                            "description": "Updated transaction",
                            "amount": 20.2,
                            "source": {
                                "id": 1
                            },
                            "destination": {
                                "id": 2
                            },
                            "category": {
                                "id": 1
                            },
                            "budget": {
                                "id": 3
                            }
                        }""")
                .post("/api/accounts/{accountId}/transactions/{transactionId}", 1, 123)
            .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(1))
                .body("description", Matchers.equalTo("Updated transaction"))
                .body("currency", Matchers.equalTo("EUR"))
                .body("type.code", Matchers.equalTo("CREDIT"))
                .body("metadata.budget", Matchers.equalTo("Household"))
                .body("dates.transaction", Matchers.equalTo("2019-01-01"))
                .body("destination.id", Matchers.equalTo(2))
                .body("source.id", Matchers.equalTo(1))
                .body("amount", Matchers.equalTo(20.2F));

        // @formatter:on

        Mockito.verify(transaction).changeAmount(20.2D, "EUR");
        Mockito.verify(transaction).changeAccount(true, account);
        Mockito.verify(transaction).changeAccount(false, toAccount);
    }

    @Test
    @DisplayName("Split a transaction with missing transaction")
    void split_noTransaction(RequestSpecification spec) {
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option());

        // @formatter:off

        spec.when()
                .body("""
                        {
                            "splits": [
                                {
                                    "description": "Part 1",
                                    "amount": -5
                                },
                                {
                                    "description": "Part 2",
                                    "amount": -15
                                }
                            ]
                        }""")
                .patch("/api/accounts/{accountId}/transactions/{transactionId}", 1, 123)
            .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("No transaction found for id 123"));

        // @formatter:on
    }

    @Test
    @DisplayName("Split a transaction")
    void split(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .name("To account")
                .type("checking")
                .currency("EUR")
                .build();
        final Account toAccount = Account.builder()
                .id(2L)
                .user(ACTIVE_USER)
                .currency("EUR")
                .type("debtor")
                .name("From account").build();

        Transaction transaction = Mockito.spy(Transaction.builder()
                .id(1L)
                .description("Sample transaction")
                .category("Grocery")
                .user(ACTIVE_USER)
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
                                .account(toAccount)
                                .amount(-20.00D)
                                .build()
                ))
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        // @formatter:off

        spec.when()
                .body("""
                        {
                            "splits": [
                                {
                                    "description": "Part 1",
                                    "amount": -5
                                },
                                {
                                    "description": "Part 2",
                                    "amount": -15
                                }
                            ]
                        }""")
                .patch("/api/accounts/{accountId}/transactions/{transactionId}", 1, 123)
            .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(1))
                .body("description", Matchers.equalTo("Sample transaction"))
                .body("currency", Matchers.equalTo("EUR"))
                .body("type.code", Matchers.equalTo("DEBIT"))
                .body("metadata.category", Matchers.equalTo("Grocery"))
                .body("metadata.budget", Matchers.equalTo("Household"))
                .body("dates.transaction", Matchers.equalTo("2019-01-15"))
                .body("destination.id", Matchers.equalTo(1))
                .body("source.id", Matchers.equalTo(2))
                .body("amount", Matchers.equalTo(20.0F))
                .body("split[0].description", Matchers.equalTo("Part 1"))
                .body("split[0].amount", Matchers.equalTo(5.0F))
                .body("split[1].description", Matchers.equalTo("Part 2"))
                .body("split[1].amount", Matchers.equalTo(15.0F));

        // @formatter:on
    }

    @Test
    @DisplayName("Delete a transaction by id")
    void delete(RequestSpecification spec) {
        Transaction transaction = Mockito.mock(Transaction.class);
        Mockito.when(transaction.getUser()).thenReturn(ACTIVE_USER);

        Account account = Account.builder()
                .id(1L)
                .name("To account")
                .type("checking")
                .user(ACTIVE_USER)
                .currency("EUR")
                .build();

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));
        Mockito.when(transactionProvider.lookup(123L)).thenReturn(Control.Option(transaction));

        // @formatter:off

        spec.when()
                .delete("/api/accounts/{accountId}/transactions/{transactionId}", 1, 123)
            .then()
                .statusCode(204);

        // @formatter:on

        Mockito.verify(transaction).delete();
    }

}
