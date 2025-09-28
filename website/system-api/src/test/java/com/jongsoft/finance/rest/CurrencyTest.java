package com.jongsoft.finance.rest;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"jpa", "h2", "test"})
public class CurrencyTest {

  @Replaces
  @MockBean
  public AuthenticationFacade authenticationFacade() {
    var mockedFacade = mock(AuthenticationFacade.class);
    when(mockedFacade.authenticated()).thenReturn("test@account.local");
    return mockedFacade;
  }

  @BeforeEach
  void setup(ApplicationEventPublisher publisher) {
    new EventBus(publisher);
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
          .post("/api/currencies")
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
        .get("/api/currencies/{code}")
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
          .patch("/api/currencies/{code}")
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
          .put("/api/currencies/{code}")
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
