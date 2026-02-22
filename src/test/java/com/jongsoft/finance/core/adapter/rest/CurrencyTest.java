package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Regression - Currencies")
@MicronautTest(environments = {"jpa", "h2", "test", "test-jpa"})
public class CurrencyTest {

  @Replaces
  @MockBean
  public AuthenticationFacade authenticationFacade() {
    var mockedFacade = mock(AuthenticationFacade.class);
    when(mockedFacade.authenticated()).thenReturn("test@account.local");
    return mockedFacade;
  }

  @Test
  @DisplayName("Create the KSH currency and validate it can be retrieved")
  void createKSHCurrency(RequestSpecification spec) {
    RestAssured.given(spec)
          .contentType(ContentType.JSON)
          .body(Map.of(
              "name", "Kenyan Shilling",
              "code", "KSH",
              "symbol", "$"))
        .when()
          .post("/v2/api/currencies")
        .then()
          .log().ifError()
          .statusCode(201)
          .body("name", equalTo("Kenyan Shilling"))
          .body("code", startsWith("KSH"))
          .body("symbol", equalTo("$"))
          .body("enabled", equalTo(true))
          .body("decimalPlaces", equalTo(2));

    // check the fetch
    RestAssured.given(spec)
        .pathParam("code", "KSH")
        .get("/v2/api/currencies/{code}")
        .then()
          .statusCode(200)
          .body("name", equalTo("Kenyan Shilling"))
          .body("code", startsWith("KSH"));

    // disable the currency
    RestAssured.given(spec)
          .contentType(ContentType.JSON)
          .body(Map.of(
              "enabled", false,
              "decimalPlaces", 4))
        .when()
          .pathParam("code", "KSH")
          .patch("/v2/api/currencies/{code}")
        .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("name", equalTo("Kenyan Shilling"))
          .body("code", equalTo("KSH"))
          .body("enabled", equalTo(false))
          .body("decimalPlaces", equalTo(4));

    // rename the currency
    RestAssured.given(spec)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "name", "Kenyan Shilling (KS)",
            "symbol", "S"))
        .when()
          .pathParam("code", "KSH")
          .put("/v2/api/currencies/{code}")
        .then()
        .log().ifValidationFails()
          .statusCode(200)
          .body("name", equalTo("Kenyan Shilling (KS)"))
          .body("code", equalTo("KSH"))
          .body("symbol", equalTo("S"))
          .body("enabled", equalTo(false))
          .body("decimalPlaces", equalTo(4));
  }
}
