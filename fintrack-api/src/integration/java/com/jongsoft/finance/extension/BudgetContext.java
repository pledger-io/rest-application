package com.jongsoft.finance.extension;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.equalTo;

public class BudgetContext {
    private final Supplier<RequestSpecification> requestSpecification;

    BudgetContext(Supplier<RequestSpecification> requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public BudgetContext create(int year, int month, double income) {
        requestSpecification.get()
                .body("""
                        {
                            "year": %d,
                            "month": %d,
                            "income": %.2f
                        }
                        """.formatted(year, month, income))
                .when()
                    .put("/budgets")
                .then()
                    .statusCode(201);
        return this;
    }

    public BudgetContext updateIncome(double amount) {
        var now = LocalDate.now();

        requestSpecification.get()
                .body("""
                        {
                            "year": %d,
                            "month": %d,
                            "income": %.2f
                        }
                        """.formatted(now.getYear(), now.getMonthValue(), amount))
                .when()
                    .patch("/budgets")
                .then()
                    .statusCode(200);

        return this;
    }

    public BudgetContext createExpense(String name, double amount) {
        requestSpecification.get()
                .body("""
                        {
                            "name": "%s",
                            "amount": %.2f
                        }
                        """.formatted(name, amount))
                .when()
                    .patch("/budgets/expenses")
                .then()
                    .statusCode(200);
        return this;
    }

    public BudgetContext updateExpense(String name, double amount) {
        var now = LocalDate.now();
        var expenseId = requestSpecification
                .get()
                    .pathParam("year", now.getYear())
                    .pathParam("month", now.getMonthValue())
                .when()
                    .get("/budgets/{year}/{month}")
                .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .jsonPath()
                    .getLong("expenses.find { it.name == '%s' }.id".formatted(name));

        requestSpecification.get()
                .body("""
                        {
                            "name": "%s",
                            "amount": %.2f,
                            "expenseId": %d
                        }
                        """.formatted(name, amount, expenseId))
                .when()
                    .patch("/budgets/expenses")
                .then()
                    .statusCode(200);
        return this;
    }

    public BudgetContext validateBudget(int year, int month, Consumer<ValidatableResponse> validator) {
        var response = requestSpecification
                .get()
                    .pathParam("year", year)
                    .pathParam("month", month)
                .when()
                    .get("/budgets/{year}/{month}")
                .then()
                    .statusCode(200)
                    .body("period.from", equalTo("%s-%02d-01".formatted(year, month)));
        validator.accept(response);
        return this;
    }
}
