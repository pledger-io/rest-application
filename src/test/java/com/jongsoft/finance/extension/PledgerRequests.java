package com.jongsoft.finance.extension;

import com.jongsoft.lang.time.Range;
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .pathParam("id", accountId)
          .when()
              .get("/v2/api/accounts/{id}/saving-goals")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse reserveMoneyForSavingGoal(long accountId, long savingGoalId, double amount) {
        return given(requestSpecification)
              .log().ifValidationFails()
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .body(Map.of("name", name, "description", description))
          .when()
              .post("/v2/api/categories")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchCategory(long id) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .get("/v2/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateCategory(long id, String name, String description) {
        return given(requestSpecification)
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .pathParam("id", id)
          .when()
              .delete("/v2/api/categories/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchCategories(int offset, int limit, String name) {
        var request = given(requestSpecification)
              .log().ifValidationFails()
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .pathParam("id", contractId)
          .when()
              .get("/v2/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateContract(long contractId, String name, String description, LocalDate startDate, LocalDate endDate) {
        return given(requestSpecification)
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .pathParam("id", contractId)
          .when()
              .post("/v2/api/contracts/{id}/warn-before-expiration")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteContract(long contractId) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .pathParam("id", contractId)
          .when()
              .delete("/v2/api/contracts/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchContracts(String name, String status) {
        var request = given(requestSpecification)
              .log().ifValidationFails();

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
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .body(Map.of("name", name))
          .when()
              .post("/v2/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchTags(String name) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .queryParam("name", name)
          .when()
              .get("/v2/api/tags")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteTag(String name) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .pathParam("name", name)
          .when()
              .delete("/v2/api/tags/{name}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchAccountTypes() {
        return given(requestSpecification)
              .log().ifValidationFails()
              .get("/v2/api/account-types")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createExport() {
        return given(requestSpecification)
              .log().ifValidationFails()
              .get("/v2/api/export")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createScheduleMonthly(long sourceAccount, long destinationAccount, String name, double amount) {
        return given(requestSpecification)
              .log().ifValidationFails()
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
              .log().ifValidationFails()
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
              .log().ifValidationFails()
              .pathParam("id", scheduleId)
          .when()
              .get("/v2/api/schedules/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteSchedule(long scheduleId) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .pathParam("id", scheduleId)
          .when()
              .delete("/v2/api/schedules/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchSchedules(List<Integer> accounts, Integer contractId) {
        var request = given(requestSpecification)
              .log().ifValidationFails();
        if (accounts != null) {
            request.queryParam("account", accounts);
        }
        if (contractId != null) {
            request.queryParam("contract", contractId);
        }

        return request.when()
              .get("/v2/api/schedules")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse createTransaction(long fromAccount, long toAccount, double amount, String currency, LocalDate date, String description) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .body(Map.of(
                    "date", date.toString(),
                    "currency", currency,
                    "description", description,
                    "amount", amount,
                    "source", fromAccount,
                    "target", toAccount))
          .when()
              .post("/v2/api/transactions")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse fetchTransaction(long id) {
        return given(requestSpecification)
              .log().ifValidationFails()
              .pathParam("id", id)
          .when()
              .get("/v2/api/transactions/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse updateTransaction(long id, long fromAccount, long toAccount, String description, double amount, LocalDate date, Long categoryId, Long expenseId, Long contractId, String tag) {
        var body = new HashMap<String, Object>();
        body.put("source", fromAccount);
        body.put("target", toAccount);
        body.put("date", date.toString());
        body.put("description", description);
        body.put("amount", amount);
        body.put("currency", "EUR");

        if (categoryId != null) {
            body.put("category", categoryId);
        }
        if (expenseId != null) {
            body.put("expense", expenseId);
        }
        if (contractId != null) {
            body.put("contract", contractId);
        }
        if (tag != null) {
            body.put("tags", List.of(tag));
        }

        return given(requestSpecification)
              .log().ifValidationFails()
              .contentType(ContentType.JSON)
              .pathParam("id", id)
              .body(body)
          .when()
              .put("/v2/api/transactions/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse deleteTransaction(long id) {
        return given(requestSpecification)
              .pathParam("id", id)
          .when()
              .delete("/v2/api/transactions/{id}")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse searchTransactionsForAccounts(int offset, int limit, LocalDate startDate, LocalDate endDate, List<Long> accountIds) {
        var request = given(requestSpecification)
              .log().ifValidationFails()
              .queryParam("offset", offset)
              .queryParam("numberOfResults", limit)
              .queryParam("startDate", startDate.toString())
              .queryParam("endDate", endDate.toString());
        if (accountIds != null) {
            request.queryParam("account", accountIds);
        }

        return request.when()
              .get("/v2/api/transactions")
          .then()
              .log().ifValidationFails();
    }

    public ValidatableResponse computeBalance(Range<LocalDate> range, List<Long> accountIds, List<Long> categories) {
        return given(requestSpecification)
                .log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "range", Map.of(
                        "startDate", range.from().toString(),
                        "endDate", range.until().toString()),
                    "accounts", accountIds,
                    "categories", categories
                ))
            .when()
                .post("/v2/api/balance")
            .then()
                .log().ifValidationFails();
    }

}
