package com.jongsoft.finance.extension;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.List;
import java.util.function.Consumer;

public class AccountContext {

    private final RequestSpecification requestSpecification;

    public class Account {
        private final int id;

        private Account(String name) {
            this.id = requestSpecification
                    .get("/accounts/all")
                .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .<Integer>getList("findAll { it.name == '%s' }.id".formatted(name))
                    .getFirst();
        }

        public Account fetch(Consumer<ValidatableResponse> validator) {
            validator.accept(requestSpecification
                    .pathParam("id", id)
                    .get("/accounts/{id}")
                .then()
                    .statusCode(200));
            return this;
        }

        public Account icon(String uploadId) {
            requestSpecification
                    .body("""
                            {
                                "fileCode": "%s"
                            }""".formatted(uploadId))
                    .pathParam("id", id)
                    .post("/accounts/{id}/image")
                .then()
                    .statusCode(200);
            return this;
        }

        long getId() {
            return id;
        }
    }

    AccountContext(RequestSpecification restAssured) {
        this.requestSpecification = restAssured;
    }

    public AccountContext create(String name, String description, String type) {
        requestSpecification
                .body("""
                        {
                            "name": "%s",
                            "description": "%s",
                            "currency": "EUR",
                            "type": "%s"
                        }
                        """.formatted(name, description, type))
                .put("/accounts")
            .then()
                .statusCode(200);

        return this;
    }

    public AccountContext own(Consumer<ValidatableResponse> validator) {
        var response = requestSpecification.get("/accounts/my-own")
                .then()
                .statusCode(200);
        validator.accept(response);
        return this;
    }

    public AccountContext debtor(String name, String description) {
        return create(name, description, "debtor");
    }

    public AccountContext debtors(Consumer<ResponseSpecification> validator) {
        validator.accept(list(List.of("debtor")));
        return this;
    }

    public AccountContext creditor(String name, String description) {
        return create(name, description, "creditor");
    }

    public AccountContext creditors(Consumer<ResponseSpecification> validator) {
        validator.accept(list(List.of("creditor")));
        return this;
    }

    public AccountContext locate(String name, Consumer<Account> account) {
        account.accept(new Account(name));
        return this;
    }

    private ResponseSpecification list(List<String> types) {
        return requestSpecification
                .body("""
                        {
                            "accountTypes": %s
                        }
                        """.formatted(types))
                .then()
                .statusCode(200);
    }
}
