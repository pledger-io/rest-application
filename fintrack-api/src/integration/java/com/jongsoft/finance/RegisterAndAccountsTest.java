package com.jongsoft.finance;

import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Step 3: User loads the account pages")
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
    @Order(4)
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
