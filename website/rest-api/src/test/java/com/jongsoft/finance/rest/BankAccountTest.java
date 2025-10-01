package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class BankAccountTest {

  @Test
  void creatingNewBankAccount(PledgerContext pledgerContext, RequestSpecification spec) {
    pledgerContext.withUser("test@account.local");

    given(spec)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "name", "Lidl Stores",
            "description", "Lidl stores in Berlin",
            "iban", "NL00ABNA0417164300",
            "currency", "EUR",
            "type", "creditor"))
      .when()
        .post("/api/accounts")
      .then()
        .log().ifValidationFails()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("Lidl Stores"))
        .body("description", equalTo("Lidl stores in Berlin"))
        .body("account.currency", equalTo("EUR"))
        .body("account.iban", equalTo("NL00ABNA0417164300"));
  }
}
