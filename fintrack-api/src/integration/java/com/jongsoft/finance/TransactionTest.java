package com.jongsoft.finance;

import com.jongsoft.finance.extension.IntegrationTest;
import com.jongsoft.finance.extension.TestContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@IntegrationTest(phase = 2)
@DisplayName("User creates the initial transactions:")
public class TransactionTest {

    @Test
    @Order(1)
    @DisplayName("Book the first salary income")
    void firstIncome(TestContext context) {
        context.authenticate("sample@e", "Zomer2020");
        var checkingAccountId = context.locateAccountId("My checking account");
        var employerId = context.locateAccountId("Chicko Cointer");

        context.authRequest()
                .body("""
                        {
                            "amount": 1000,
                            "description": "First income",
                            "date": "2020-01-01",
                            "currency": "EUR",
                            "source": {
                                "id": %s
                            },
                            "destination": {
                                "id": %s
                            }
                        }
                        """.formatted(checkingAccountId, employerId))
                .pathParam("id", checkingAccountId)
                .put("/accounts/{id}/transactions")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
