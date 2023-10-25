package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.AccountTypeProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

@MicronautTest
@DisplayName("Account list / create resource")
class AccountResourceTest extends TestSetup {

    @Inject
    private AccountTypeProvider accountTypeProvider;

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private FilterFactory filterFactory;


    @BeforeEach
    void setup() {
        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @MockBean
    @Replaces
    private AccountTypeProvider accountTypeProvider() {
        return Mockito.mock(AccountTypeProvider.class);
    }

    @Test
    @DisplayName("Fetch own accounts")
    void ownAccounts(RequestSpecification spec) {
        var resultPage = Mockito.mock(ResultPage.class);
        Mockito.when(resultPage.content()).thenReturn(Collections.List(
                Account.builder()
                        .id(1L)
                        .name("Sample account")
                        .description("Long description")
                        .iban("NL123INGb23039283")
                        .currency("EUR")
                        .balance(2000.2D)
                        .firstTransaction(LocalDate.of(2019, 1, 1))
                        .lastTransaction(LocalDate.of(2022, 3, 23))
                        .type("checking")
                        .build()));

        Mockito.when(accountTypeProvider.lookup(false)).thenReturn(Collections.List("default", "savings"));
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class))).thenReturn(resultPage);

        // @formatter:off
        spec.when()
                .get("/api/accounts/my-own")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].name", CoreMatchers.equalTo("Sample account"))
                .body("[0].description", CoreMatchers.equalTo("Long description"))
                .body("[0].account.iban", CoreMatchers.equalTo("NL123INGb23039283"));
        // @formatter:on
    }

    @Test
    @DisplayName("Fetch all accounts")
    void allAccounts(RequestSpecification spec) {
        var resultPage = Collections.List(Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("creditor")
                .build());

        Mockito.when(accountProvider.lookup())
                .thenReturn(resultPage);

        // @formatter:off
        spec.when()
                .get("/api/accounts/all")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].name", CoreMatchers.equalTo("Sample account"))
                .body("[0].description", CoreMatchers.equalTo("Long description"))
                .body("[0].account.iban", CoreMatchers.equalTo("NL123INGb23039283"));
        // @formatter:on

        Mockito.verify(accountProvider).lookup();
    }

    @Test
    @DisplayName("Autocomplete accounts with token and type")
    void autocomplete(RequestSpecification spec) {
        var resultPage = Mockito.mock(ResultPage.class);
        Mockito.when(resultPage.content()).thenReturn(Collections.List(
                Account.builder()
                        .id(1L)
                        .name("Sample account")
                        .description("Long description")
                        .iban("NL123INGb23039283")
                        .currency("EUR")
                        .balance(2000.2D)
                        .firstTransaction(LocalDate.of(2019, 1, 1))
                        .lastTransaction(LocalDate.of(2022, 3, 23))
                        .type("checking")
                        .build()));

        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        // @formatter:off
        spec.when()
                .get("/api/accounts/auto-complete?token=sampl&type=creditor")
            .then()
                .statusCode(200)
                .body("$", Matchers.hasSize(1))
                .body("[0].name", CoreMatchers.equalTo("Sample account"))
                .body("[0].description", CoreMatchers.equalTo("Long description"))
                .body("[0].account.iban", CoreMatchers.equalTo("NL123INGb23039283"));
        // @formatter:on

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockCommand).name("sampl", false);
        Mockito.verify(mockCommand).types(Collections.List("creditor"));
    }

    @Test
    @DisplayName("Search accounts with type creditor")
    void accounts_creditor(RequestSpecification spec) {
        var resultPage = ResultPage.of(Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("creditor")
                .build());

        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        // @formatter:off
        spec.when()
                .body(AccountSearchRequest.builder()
                    .accountTypes(List.of("creditor"))
                    .page(0)
                    .build())
                .post("/api/accounts")
            .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("content[0].name", CoreMatchers.equalTo("Sample account"))
                .body("content[0].description", CoreMatchers.equalTo("Long description"))
                .body("content[0].account.iban", CoreMatchers.equalTo("NL123INGb23039283"));
        // @formatter:on

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockCommand).types(Collections.List("creditor"));
    }

    @Test
    @DisplayName("Create account")
    void create(RequestSpecification spec) {
        var account = Mockito.spy(Account.builder()
                .id(1L)
                .balance(0D)
                .name("Sample account")
                .type("checking")
                .currency("EUR")
                .build());

        Mockito.when(accountProvider.lookup("Sample account"))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(account));

        var request = AccountEditRequest.builder()
                .name("Sample account")
                .currency("EUR")
                .type("checking")
                .iban("NL45RABO1979747032")
                .interest(0.22)
                .interestPeriodicity(Periodicity.MONTHS)
                .build();

        // @formatter:off
        spec.when()
                .body(request)
                .put("/api/accounts")
            .then()
                .statusCode(200)
                .body("name", CoreMatchers.equalTo("Sample account"))
                .body("description", CoreMatchers.nullValue())
                .body("account.iban", CoreMatchers.equalTo("NL45RABO1979747032"))
                .body("account.bic", CoreMatchers.nullValue())
                .body("account.number", CoreMatchers.nullValue())
                .body("history.firstTransaction", CoreMatchers.nullValue())
                .body("history.lastTransaction", CoreMatchers.nullValue())
                .body("type", CoreMatchers.equalTo("checking"))
                .body("account.currency", CoreMatchers.equalTo("EUR"))
                .body("interest.interest", CoreMatchers.equalTo(0.22F))
                .body("interest.periodicity", CoreMatchers.equalTo("MONTHS"));
        // @formatter:on

        Mockito.verify(accountProvider, Mockito.times(2)).lookup("Sample account");
        Mockito.verify(account).interest(0.22, Periodicity.MONTHS);
    }

}
