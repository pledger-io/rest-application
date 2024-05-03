package com.jongsoft.finance;

import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

@IntegrationTest(phase = 1)
@DisplayName("User registers and creates accounts:")
public class RegisterAndAccountsTest {

    @Test
    @Order(1)
    @DisplayName("Step 1: Create and authenticate.")
    void createAccount(TestContext context) {
        context
                .register("sample@e", "Zomer2020")
                .authenticate("sample@e", "Zomer2020");
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: User creates the accounts")
    void setupAccounts(TestContext context) {
        context.accounts()
                .create("My checking account", "This is my first account", "default")
                .debtor("Chicko Cointer", "The employer of the person")
                .creditor("Netflix", "Movie subscription service")
                .creditor("Guarda", "A nice little shop.")
                .creditor("Groceries are us", "A grocery shop.");
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: User adds a budget")
    void addBudget(TestContext context) {
        var now = LocalDate.now();
        context.budgets()
                .create(2021, 1, 1000.00)
                .createExpense("Rent", 500.00)
                .createExpense("Groceries", 200.00)
                .validateBudget(2021, 1, budget -> budget
                        .body("income", equalTo(1000.00F))
                        .body("expenses.size()", equalTo(2))
                        .body("expenses.name", hasItems("Rent", "Groceries"))
                        .body("expenses.expected", hasItems(500.00F, 200F)))
                .updateIncome(2200)
                .updateExpense("Rent", 600.00)
                .updateExpense("Groceries", 250.00)
                .createExpense("Car", 300.00)
                .validateBudget(now.getYear(), now.getMonthValue(), budget -> budget
                        .body("income", equalTo(2200.00F))
                        .body("expenses.size()", equalTo(3))
                        .body("expenses.name", hasItems("Rent", "Groceries", "Car"))
                        .body("expenses.expected", hasItems(600.00F, 250.00F, 300.00F)))
                .validateBudget(2021, 1, budget -> budget
                        .body("income", equalTo(1000.00F))
                        .body("period.until", equalTo(now.withDayOfMonth(1).toString()))
                        .body("expenses.size()", equalTo(2))
                        .body("expenses.name", hasItems("Rent", "Groceries"))
                        .body("expenses.expected", hasItems(500.00F, 200F)));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: User loads the account pages")
    void validateAccount(TestContext context) {
        context.accounts()
                .own(response -> response
                        .body("size()", equalTo(1))
                        .body("[0].name", equalTo("My checking account")))
                .creditors(response -> response
                        .body("info.records", equalTo(3))
                        .body("content.name", hasItems("Groceries are us", "Guarda")))
                .debtors(response -> response
                        .body("info.records", equalTo(1))
                        .body("content.name", hasItems("Chicko Cointer")));
    }

    @Test
    @Order(5)
    @DisplayName("Step 4: Update the shopping account with image")
    void editShoppingAccount(TestContext context) {
        var uploadId = context.upload(RegisterAndAccountsTest.class.getResourceAsStream("/assets/account1.svg"));

        context.accounts()
                .locate("Groceries are us", account -> account
                        .fetch(response -> response
                                .body("name", equalTo("Groceries are us"))
                                .body("iconFileCode", nullValue()))
                        .icon(uploadId)
                        .fetch(response -> response
                                .body("name", equalTo("Groceries are us"))
                                .body("iconFileCode", equalTo(uploadId))));
    }
}
