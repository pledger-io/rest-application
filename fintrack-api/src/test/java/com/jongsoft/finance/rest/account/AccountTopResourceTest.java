package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
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

@DisplayName("Account Top Resource")
class AccountTopResourceTest extends TestSetup {

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private FilterFactory filterFactory;

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Test
    @DisplayName("Compute the top debtors")
    void topDebtors(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("checking")
                .build();

        Mockito.doReturn(Collections.List(new AccountProvider.AccountSpending() {
            @Override
            public Account account() {
                return account;
            }

            @Override
            public double total() {
                return 1200D;
            }

            @Override
            public double average() {
                return 50D;
            }
        })).when(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(true));

        // @formatter:off

        spec.when()
                .get("/api/accounts/top/debit/2019-01-01/2019-02-01")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].account.id", Matchers.equalTo(1))
                .body("[0].account.name", Matchers.equalTo("Sample account"))
                .body("[0].account.description", Matchers.equalTo("Long description"))
                .body("[0].account.account.iban", Matchers.equalTo("NL123INGb23039283"))
                .body("[0].account.account.currency", Matchers.equalTo("EUR"))
                .body("[0].account.history.firstTransaction", Matchers.equalTo("2019-01-01"))
                .body("[0].account.history.lastTransaction", Matchers.equalTo("2022-03-23"))
                .body("[0].account.type", Matchers.equalTo("checking"))
                .body("[0].total", Matchers.equalTo(1200F))
                .body("[0].average", Matchers.equalTo(50F));

        // @formatter:on

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(true));
        Mockito.verify(mockCommand).types(Collections.List("debtor"));
    }

    @Test
    @DisplayName("Compute the top creditors")
    void topCreditor(RequestSpecification spec) {
        Account account = Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("checking")
                .build();

        Mockito.doReturn(Collections.List(new AccountProvider.AccountSpending() {
            @Override
            public Account account() {
                return account;
            }

            @Override
            public double total() {
                return 1200D;
            }

            @Override
            public double average() {
                return 50D;
            }
        })).when(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(false));

        // @formatter:off

        spec.when()
                .get("/api/accounts/top/creditor/2019-01-01/2019-02-01")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].account.id", Matchers.equalTo(1))
                .body("[0].account.name", Matchers.equalTo("Sample account"))
                .body("[0].account.description", Matchers.equalTo("Long description"))
                .body("[0].account.account.iban", Matchers.equalTo("NL123INGb23039283"))
                .body("[0].account.account.currency", Matchers.equalTo("EUR"))
                .body("[0].account.history.firstTransaction", Matchers.equalTo("2019-01-01"))
                .body("[0].account.history.lastTransaction", Matchers.equalTo("2022-03-23"))
                .body("[0].account.type", Matchers.equalTo("checking"))
                .body("[0].total", Matchers.equalTo(1200F))
                .body("[0].average", Matchers.equalTo(50F));

        // @formatter:on

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(false));
        Mockito.verify(mockCommand).types(Collections.List("creditor"));
    }

}
