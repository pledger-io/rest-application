package com.jongsoft.finance.extension;

import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.time.LocalDate;

public class TransactionContext {

    private final RequestSpecification requestSpecification;

    public TransactionContext(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public TransactionContext create(
            AccountContext.Account from,
            AccountContext.Account to,
            double amount,
            String description,
            LocalDate date) {
        requestSpecification
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
                .pathParam("id", from.getId())
                .put("/accounts/{id}/transactions")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        return this;
    }
}
