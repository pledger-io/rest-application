package com.jongsoft.finance.rest.extension;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class PledgerRequests {
    private final RequestSpecification requestSpecification;

    public PledgerRequests(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public ValidatableResponse createBankAccount(Map<String, String> bankAccount) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(bankAccount)
          .when()
              .post("/api/accounts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateBankAccount(long id, Map<String, String> bankAccount) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(bankAccount)
          .when()
              .put("/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchBankAccount(long id) {
        return given(requestSpecification)
              .pathParam("id", id)
          .when()
              .get("/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteBankAccount(long id) {
        return given(requestSpecification)
              .pathParam("id", id)
          .when()
              .delete("/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createSavingGoal(long id, String name, double goal, LocalDate targetDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(Map.of("name", name, "goal", goal, "targetDate", targetDate.toString()))
          .when()
            .post("/api/accounts/{id}/saving-goals")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateSavingGoal(long accountId, long savingGoalId, double goal, LocalDate targetDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", accountId)
              .pathParam("savingGoalId", savingGoalId)
              .body(Map.of("goal", goal, "targetDate", targetDate.toString()))
          .when()
              .put("/api/accounts/{id}/saving-goals/{savingGoalId}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchSavingGoals(long accountId) {
        return given(requestSpecification)
              .pathParam("id", accountId)
          .when()
              .get("/api/accounts/{id}/saving-goals")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse reserveMoneyForSavingGoal(long accountId, long savingGoalId, double amount) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", accountId)
              .pathParam("goal-id", savingGoalId)
              .body(Map.of("amount", amount))
          .when()
              .post("/api/accounts/{id}/saving-goals/{goal-id}/make-reservation")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteSavingGoal(long accountId, long savingGoalId) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", accountId)
              .pathParam("savingGoalId", savingGoalId)
          .when()
              .delete("/api/accounts/{id}/saving-goals/{savingGoalId}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchBankAccounts(int offset, int limit, List<String> type, String accountName) {
        var request = given(requestSpecification)
              .queryParam("offset", offset)
              .queryParam("numberOfResults", limit);

        if (!type.isEmpty()) {
            request.queryParam("type", type);
        }
        if (accountName != null) {
            request.queryParam("accountName", accountName);
        }

        return request.when()
              .get("/api/accounts")
          .then()
              .log().ifValidationFails();
    }
}
