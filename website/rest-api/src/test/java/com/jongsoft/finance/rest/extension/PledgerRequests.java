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

    public ValidatableResponse createCategory(String name, String description) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of("name", name, "description", description))
          .when()
              .post("/api/categories")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchCategory(long id) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .get("/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateCategory(long id, String name, String description) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(Map.of("name", name, "description", description))
          .when()
              .put("/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteCategory(long id) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .delete("/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchCategories(int offset, int limit, String name) {
        var request = given(requestSpecification)
              .queryParam("offset", offset)
              .queryParam("numberOfResults", limit);
        if (name != null) {
            request.queryParam("name", name);
        }

        return request.when()
              .get("/api/categories")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createContract(long accountId, String name, String description, LocalDate startDate, LocalDate endDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of(
                    "company", Map.of("id", accountId),
                    "name", name,
                    "description", description,
                    "start", startDate.toString(),
                    "end", endDate.toString()
              ))
          .when()
              .post("/api/contracts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchContract(long contractId) {
        return given(requestSpecification)
              .pathParam("id", contractId)
          .when()
              .get("/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateContract(long contractId, String name, String description, LocalDate startDate, LocalDate endDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", contractId)
              .body(Map.of(
                    "name", name,
                    "description", description,
                    "start", startDate.toString(),
                    "end", endDate.toString()
              ))
          .when()
              .put("/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse warnBeforeContractExpires(long contractId) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", contractId)
          .when()
              .post("/api/contracts/{id}/warn-before-expiration")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteContract(long contractId) {
        return given(requestSpecification)
              .pathParam("id", contractId)
          .when()
              .delete("/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchContracts(String name) {
        return given(requestSpecification)
              .queryParam("name", name)
          .when()
              .get("/api/contracts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createTag(String name) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of("name", name))
          .when()
              .post("/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchTags(String name) {
        return given(requestSpecification)
              .queryParam("name", name)
          .when()
              .get("/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteTag(String name) {
        return given(requestSpecification)
              .pathParam("name", name)
          .when()
              .delete("/api/tags/{name}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchAccountTypes() {
        return given(requestSpecification)
              .get("/api/account-types")
          .then()
              .log().ifValidationFails();
    }

}
