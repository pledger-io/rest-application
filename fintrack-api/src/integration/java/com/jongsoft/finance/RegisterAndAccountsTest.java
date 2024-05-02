package com.jongsoft.finance;

import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

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
        context.authRequest()
                .body("""
                        {
                            "name": "My checking account",
                            "description": "This is my first account",
                            "currency": "EUR",
                            "type": "default"
                        }
                        """)
                .put("/accounts")
                .then()
                .statusCode(200);

        context.authRequest()
                .body("""
                        {
                            "name": "Chicko Cointer",
                            "description": "The employer of the person",
                            "currency": "EUR",
                            "type": "debtor"
                        }
                        """)
                .put("/accounts")
                .then()
                .statusCode(200);

        context.authRequest()
                .body("""
                        {
                            "name": "Netflix",
                            "description": "Movie subscription service",
                            "currency": "EUR",
                            "type": "creditor"
                        }
                        """)
                .put("/accounts")
                .then()
                .statusCode(200);

        context.authRequest()
                .body("""
                        {
                            "name": "Guarda",
                            "description": "A nice little shop.",
                            "currency": "EUR",
                            "type": "creditor"
                        }
                        """)
                .put("/accounts")
                .then()
                .statusCode(200);

        context.authRequest()
                .body("""
                        {
                            "name": "Groceries are us",
                            "description": "A grocery shop.",
                            "currency": "EUR",
                            "type": "creditor"
                        }
                        """)
                .put("/accounts")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: User loads the account pages")
    void validateAccount(TestContext context) {
        context.authRequest()
                .get("/accounts/my-own")
                .then()
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("My checking account"));

        context.authRequest()
                .body("""
                        {
                            "accountTypes": ["debtor"]
                        }
                        """)
                .post("/accounts")
                .then()
                .statusCode(200)
                .body("info.records", equalTo(1))
                .body("content[0].name", equalTo("Chicko Cointer"));

        context.authRequest()
                .body("""
                        {
                            "accountTypes": ["creditor"]
                        }
                        """)
                .post("/accounts")
                .then()
                .statusCode(200)
                .body("info.records", equalTo(3))
                .body("content.name", hasItems("Groceries are us", "Guarda"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Update the shopping account with image")
    void editShoppingAccount(TestContext context) {
        var shoppingAccountId = context.locateAccountId("Groceries are us");

        var uploadId = context.upload(RegisterAndAccountsTest.class.getResourceAsStream("/assets/account1.svg"));

        context.authRequest()
                .body("""
                        {
                            "fileCode": "%s"
                        }""".formatted(uploadId))
                .pathParam("id", shoppingAccountId)
                .post("/accounts/{id}/image")
                .then()
                .statusCode(200);

        context.authRequest()
                .pathParam("id", shoppingAccountId)
                .get("/accounts/{id}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Groceries are us"))
                .body("iconFileCode", equalTo(uploadId));
    }
}
