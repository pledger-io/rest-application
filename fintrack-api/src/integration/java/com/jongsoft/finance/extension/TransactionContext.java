package com.jongsoft.finance.extension;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TransactionContext {

  public record TransactionSplit(String description, double amount) {
  }

  private final Supplier<RequestSpecification> requestSpecification;

  public TransactionContext(Supplier<RequestSpecification> requestSpecification) {
    this.requestSpecification = requestSpecification;
  }

  public TransactionContext create(
      AccountContext.Account from,
      AccountContext.Account to,
      double amount,
      String description,
      LocalDate date) {
    requestSpecification
        .get()
          .body("""
              {
                  "amount": %f,
                  "description": "%s",
                  "date": "%s",
                  "currency": "EUR",
                  "source": {
                      "id": %d
                  },
                  "destination": {
                      "id": %d
                  }
              }
              """.formatted(amount, description, date, from.getId(), to.getId()))
        .when()
          .pathParam("id", from.getId())
          .put("/accounts/{id}/transactions")
        .then()
         .statusCode(HttpStatus.SC_NO_CONTENT);
    return this;
  }

  public TransactionContext split(
      Long accountId,
      Long transactionId,
      List<TransactionSplit> splits) {
    requestSpecification
        .get()
          .body("""
              {
                "splits": [%s]
              }""".formatted(
              splits.stream()
                  .map(split -> """
                      {
                        "description": "%s",
                        "amount": %.2f
                      }""".formatted(split.description(), split.amount()))
                  .collect(Collectors.joining(", "))))
        .when()
          .pathParam("id", accountId)
          .pathParam("transactionId", transactionId)
          .patch("/accounts/{id}/transactions/{transactionId}")
        .then()
          .statusCode(HttpStatus.SC_OK);
    return this;
  }

  public TransactionContext list(LocalDate onDate, Consumer<ValidatableResponse> validator) {
    var response = requestSpecification
        .get()
        .body("""
            {
                "dateRange": {
                    "start": "%s",
                    "end": "%s"
                },
                "page": 0
            }""".formatted(onDate, onDate.plusDays(1)))
        .when()
        .post("/transactions")
        .then()
        .statusCode(HttpStatus.SC_OK);
    validator.accept(response);
    return this;
  }
}
