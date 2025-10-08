package com.jongsoft.finance.rest.extension;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.HashMap;
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
              .post("/v2/api/accounts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateBankAccount(long id, Map<String, String> bankAccount) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(bankAccount)
          .when()
              .put("/v2/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchBankAccount(long id) {
        return given(requestSpecification)
              .pathParam("id", id)
          .when()
              .get("/v2/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteBankAccount(long id) {
        return given(requestSpecification)
              .pathParam("id", id)
          .when()
              .delete("/v2/api/accounts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createSavingGoal(long id, String name, double goal, LocalDate targetDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(Map.of("name", name, "goal", goal, "targetDate", targetDate.toString()))
          .when()
            .post("/v2/api/accounts/{id}/saving-goals")
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
              .put("/v2/api/accounts/{id}/saving-goals/{savingGoalId}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchSavingGoals(long accountId) {
        return given(requestSpecification)
              .pathParam("id", accountId)
          .when()
              .get("/v2/api/accounts/{id}/saving-goals")
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
              .post("/v2/api/accounts/{id}/saving-goals/{goal-id}/make-reservation")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteSavingGoal(long accountId, long savingGoalId) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", accountId)
              .pathParam("savingGoalId", savingGoalId)
          .when()
              .delete("/v2/api/accounts/{id}/saving-goals/{savingGoalId}")
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
              .get("/v2/api/accounts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createCategory(String name, String description) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of("name", name, "description", description))
          .when()
              .post("/v2/api/categories")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchCategory(long id) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .get("/v2/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateCategory(long id, String name, String description) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(Map.of("name", name, "description", description))
          .when()
              .put("/v2/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteCategory(long id) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .delete("/v2/api/categories/{id}")
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
              .get("/v2/api/categories")
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
              .post("/v2/api/contracts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchContract(long contractId) {
        return given(requestSpecification)
              .pathParam("id", contractId)
          .when()
              .get("/v2/api/contracts/{id}")
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
              .put("/v2/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse warnBeforeContractExpires(long contractId) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", contractId)
          .when()
              .post("/v2/api/contracts/{id}/warn-before-expiration")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteContract(long contractId) {
        return given(requestSpecification)
              .pathParam("id", contractId)
          .when()
              .delete("/v2/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchContracts(String name, String status) {
        var request = given(requestSpecification);

        if (name != null) {
            request.queryParam("name", name);
        }
        if (status != null) {
            request.queryParam("status", status);
        }

        return request.when()
              .get("/v2/api/contracts")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createTag(String name) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of("name", name))
          .when()
              .post("/v2/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchTags(String name) {
        return given(requestSpecification)
              .queryParam("name", name)
          .when()
              .get("/v2/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteTag(String name) {
        return given(requestSpecification)
              .pathParam("name", name)
          .when()
              .delete("/v2/api/tags/{name}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchAccountTypes() {
        return given(requestSpecification)
              .get("/v2/api/account-types")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createExport() {
        return given(requestSpecification)
              .get("/v2/api/export")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createScheduleMonthly(long sourceAccount, long destinationAccount, String name, double amount) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .body(Map.of(
                    "name", name,
                    "amount", amount,
                    "schedule", Map.of(
                          "interval", 1,
                          "periodicity", "MONTHS"),
                    "transferBetween", Map.of(
                          "source", Map.of("id", sourceAccount),
                          "destination", Map.of("id", destinationAccount)
                    )))
          .when()
              .post("/v2/api/schedules")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse patchScheduleDateRange(long scheduleId, LocalDate startDate, LocalDate endDate) {
        return given(requestSpecification)
              .contentType(ContentType.JSON)
              .pathParam("id", scheduleId)
              .body(Map.of(
                    "activeBetween", Map.of(
                        "startDate", startDate.toString(),
                        "endDate", endDate.toString())))
          .when()
              .patch("/v2/api/schedules/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchSchedule(long scheduleId) {
        return given(requestSpecification)
              .pathParam("id", scheduleId)
          .when()
              .get("/v2/api/schedules/{id}")
          .then()
              .log().ifValidationFails();
    }

}
