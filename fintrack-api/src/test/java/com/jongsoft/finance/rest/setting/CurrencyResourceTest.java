package com.jongsoft.finance.rest.setting;

import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.providers.CurrencyProvider;
import com.jongsoft.finance.rest.TestSetup;
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

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Currency resource")
class CurrencyResourceTest extends TestSetup {

    @Inject
    private CurrencyProvider currencyProvider;

    @Replaces
    @MockBean
    private CurrencyProvider currencyProvider() {
        return Mockito.mock(CurrencyProvider.class);
    }

    @Test
    @DisplayName("List the available currencies")
    void available(RequestSpecification spec) {
        when(currencyProvider.lookup()).thenReturn(
                Collections.List(
                        Currency.builder()
                                .id(1L)
                                .name("Euro")
                                .code("EUR")
                                .symbol('E')
                                .build(),
                        Currency.builder()
                                .id(2L)
                                .name("Dollar")
                                .code("USD")
                                .symbol('D')
                                .build(),
                        Currency.builder()
                                .id(3L)
                                .name("Kwatsch")
                                .code("KWS")
                                .symbol('K')
                                .build()
                ));

        // @formatter:off
        spec
            .when()
                .get("/api/settings/currencies")
            .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("code", Matchers.hasItems("EUR", "USD", "KWS"))
                .body("name", Matchers.hasItems("Euro", "Dollar", "Kwatsch"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a new currency")
    void create(RequestSpecification spec) {
        when(currencyProvider.lookup("TCC")).thenReturn(Control.Option());

        // @formatter:off
        spec
            .given()
                .contentType("application/json")
                .body(Map.of(
                        "code", "TCC",
                        "symbol", "S",
                        "name", "Test currency"))
            .when()
                .put("/api/settings/currencies")
            .then()
                .statusCode(201)
                .body("code", Matchers.equalTo("TCC"))
                .body("name", Matchers.equalTo("Test currency"))
                .body("symbol", Matchers.equalTo("S"));
        // @formatter:on
    }

    @Test
    @DisplayName("Get a currency")
    void get(RequestSpecification spec) {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        // @formatter:off
        spec
            .when()
                .get("/api/settings/currencies/EUR")
            .then()
                .statusCode(200)
                .body("code", Matchers.equalTo("EUR"))
                .body("name", Matchers.equalTo("Euro"));
        // @formatter:on
    }

    @Test
    @DisplayName("Update a currency")
    void update(RequestSpecification spec) {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        // @formatter:off
        spec
            .given()
                .contentType("application/json")
                .body(Map.of(
                        "code", "TCC",
                        "symbol", "S",
                        "name", "Test currency"))
            .when()
                .post("/api/settings/currencies/EUR")
            .then()
                .statusCode(200)
                .body("code", Matchers.equalTo("TCC"))
                .body("name", Matchers.equalTo("Test currency"))
                .body("symbol", Matchers.equalTo("S"));
        // @formatter:on

        verify(currency).rename("Test currency", "TCC", 'S');
    }

    @Test
    @DisplayName("Patch a currency")
    void patch(RequestSpecification spec) {
        var currency = Mockito.spy(Currency.builder()
                .id(1L)
                .enabled(true)
                .decimalPlaces(3)
                .name("Euro")
                .code("EUR")
                .build());

        when(currencyProvider.lookup("EUR")).thenReturn(Control.Option(currency));

        // @formatter:off
        spec
            .given()
                .contentType("application/json")
                .body(Map.of(
                        "enabled", false,
                        "decimalPlaces", 2))
            .when()
                .patch("/api/settings/currencies/EUR")
            .then()
                .statusCode(200)
                .body("code", Matchers.equalTo("EUR"))
                .body("name", Matchers.equalTo("Euro"));
        // @formatter:on

        verify(currency).disable();
        verify(currency).accuracy(2);
    }
}
