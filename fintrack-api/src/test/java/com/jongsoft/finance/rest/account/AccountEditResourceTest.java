package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

@DisplayName("Account edit resource")
class AccountEditResourceTest extends TestSetup {
    private static final String CREATE_ACCOUNT_JSON = """
            {
                "name": "Sample account",
                "currency": "EUR",
                "type": "checking"
            }""";

    @Inject
    private AccountProvider accountProvider;

    @BeforeEach
    void setup() {
        Mockito.when(accountProvider.lookup(Mockito.anyLong())).thenReturn(Control.Option());
    }

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @AfterEach
    void after() {
        Mockito.reset(accountProvider);
    }

    @Test
    @DisplayName("Fetch a missing account")
    void get_missing(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .get("/api/accounts/{id}", "1")
            .then()
                .statusCode(404)
                .body("message", CoreMatchers.equalTo("Account not found"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create account")
    void get(RequestSpecification spec) {
        // @formatter:off
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(Account.builder()
                        .id(1L)
                        .user(ACTIVE_USER_IDENTIFIER)
                        .balance(0D)
                        .name("Sample account")
                        .currency("EUR")
                        .build()));

        spec.when()
                .get("/api/accounts/{id}", "123")
            .then()
                .statusCode(200)
                .body("name", CoreMatchers.equalTo("Sample account"));

        // @formatter:on

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    @DisplayName("Update a missing account")
    void update_missing(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .body(CREATE_ACCOUNT_JSON)
                .post("/api/accounts/{id}", "1")
            .then()
                .statusCode(404)
                .body("message", CoreMatchers.equalTo("No account found with id 1"));
        // @formatter:on

        Mockito.verify(accountProvider).lookup(1L);
    }

    @Test
    @DisplayName("Update account with valid data")
    void update(RequestSpecification spec) {
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(Account.builder()
                        .id(1L)
                        .user(ACTIVE_USER_IDENTIFIER)
                        .balance(0D)
                        .name("Sample account")
                        .currency("EUR")
                        .build()));

        // @formatter:off
        spec.when()
                .body(CREATE_ACCOUNT_JSON)
                .post("/api/accounts/{id}", "123")
            .then()
                .statusCode(200)
                .body("name", CoreMatchers.equalTo("Sample account"));
        // @formatter:on

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    @DisplayName("Set account image")
    void updateIcon(RequestSpecification spec) {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER_IDENTIFIER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

        // @formatter:off
        spec.when()
                .body(new AccountImageRequest("file-code"))
                .post("/api/accounts/{id}/image", "1")
            .then()
                .statusCode(200)
                .body("iconFileCode", CoreMatchers.equalTo("file-code"));
        // @formatter:on

        Mockito.verify(account).registerIcon("file-code");
    }

    @Test
    @DisplayName("Delete existing account")
    void delete(RequestSpecification spec) {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER_IDENTIFIER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .build());
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(account));

        // @formatter:off
        spec.when()
                .delete("/api/accounts/{id}", "123")
            .then()
                .statusCode(200);
        // @formatter:on

        Mockito.verify(account).terminate();
    }

    @Test
    @DisplayName("Create saving goal")
    void createSavingGoal(RequestSpecification spec) {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER_IDENTIFIER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .type("savings")
                .savingGoals(Collections.Set(
                        SavingGoal.builder()
                                .id(132L)
                                .name("Saving for washer")
                                .goal(BigDecimal.valueOf(1000))
                                .targetDate(LocalDate.now().plusDays(300))
                                .build()))
                .build());

        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(account));

        // @formatter:off
        spec.when()
                .body("""
                        {
                            "goal": 1500,
                            "targetDate": "%s",
                            "name": "Saving for washer"
                        }""".formatted(LocalDate.now().plusDays(300)))
                .post("/api/accounts/{id}/savings", "123")
            .then()
                .statusCode(200)
                .assertThat()
                .body("name", CoreMatchers.equalTo("Sample account"))
                .body("savingGoals.name", CoreMatchers.hasItem("Saving for washer"));
        // @formatter:on

        Mockito.verify(account).createSavingGoal(
                "Saving for washer",
                BigDecimal.valueOf(1500),
                LocalDate.now().plusDays(300));
    }
}
