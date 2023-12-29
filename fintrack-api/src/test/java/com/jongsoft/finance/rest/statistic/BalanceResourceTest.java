package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

@DisplayName("Statistic: Balance")
class BalanceResourceTest extends TestSetup {

    private BalanceResource subject;

    @Inject
    private TransactionProvider transactionProvider;
    @Inject
    private AccountProvider accountProvider;

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

    @BeforeEach
    void setup() {
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());
        Mockito.when(transactionProvider.daily(Mockito.any())).thenReturn(Collections.List());
        Mockito.when(transactionProvider.monthly(Mockito.any())).thenReturn(Collections.List());
    }

    @Test
    @DisplayName("Calculate balance")
    void calculate(RequestSpecification spec) {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        // @formatter:off
        spec
            .given()
                .body(request)
            .when()
                .post("/api/statistics/balance")
            .then()
                .statusCode(200)
                .body("balance", Matchers.equalTo(0.0F));
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).balance(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    @DisplayName("Calculate daily balance")
    void daily(RequestSpecification spec) {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        // @formatter:off
        spec
            .given()
                .body(request)
            .when()
                .post("/api/statistics/balance/daily")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(0));
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).daily(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    @DisplayName("Calculate monthly balance")
    void monthly(RequestSpecification spec) {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        // @formatter:off
        spec
            .given()
                .body(request)
            .when()
                .post("/api/statistics/balance/monthly")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(0));
        // @formatter:on

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).monthly(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    @DisplayName("Calculate partitioned balance")
    void calculatePartitioned(RequestSpecification spec) {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        Mockito.when(accountProvider.lookup()).thenReturn(Collections.List());

        // @formatter:off
        spec
            .given()
                .body(request)
            .when()
                .post("/api/statistics/balance/partitioned/{partitionKey}", "account")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(1))
                .body("[0].balance", Matchers.equalTo(0.0f));
        // @formatter:on
    }
}
