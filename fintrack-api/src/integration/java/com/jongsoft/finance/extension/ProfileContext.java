package com.jongsoft.finance.extension;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProfileContext {
    private final Supplier<RequestSpecification> requestSpecification;

    public ProfileContext(Supplier<RequestSpecification> requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public ProfileContext get(Consumer<ValidatableResponse> validator) {
        var response = requestSpecification.get()
            .when()
                .get("/profile")
            .then()
                .statusCode(HttpStatus.SC_OK);

        validator.accept(response);

        return this;
    }
}
