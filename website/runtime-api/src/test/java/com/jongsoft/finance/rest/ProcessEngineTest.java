package com.jongsoft.finance.rest;

import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"h2", "test"})
public class ProcessEngineTest {

  @MockBean(AuthenticationFacade.class)
  AuthenticationFacade authenticationFacade() {
    var mockedFacade = mock(AuthenticationFacade.class);
    when(mockedFacade.authenticated()).thenReturn("test@account.local");
    return mockedFacade;
  }

  @Test
  @DisplayName("Start an account reconcile, fetch it and cancel the process")
  void performAccountReconcileProcess(RequestSpecification spec) {
    // Create the account reconcile process
    int id = RestAssured.given(spec)
          .contentType(ContentType.JSON)
          .body(Map.of(
              "accountId", 1L,
              "startDate", "2024-01-01",
              "endDate", "2023-12-31",
              "openBalance", "10.0",
              "endBalance", "100.0"))
        .when()
          .pathParam("processDefinition", "AccountReconcile")
          .post("/api/runtime-engine/{processDefinition}")
        .then()
          .log().ifError()
          .statusCode(201)
          .body("state", equalTo("ACTIVE"))
          .body("process", startsWith("AccountReconcile:1"))
        .extract()
          .jsonPath().getInt("id");

    // verify the process exists in the system
    RestAssured.given(spec)
          .pathParam("processDefinition", "AccountReconcile")
          .pathParam("instanceId", id)
        .when()
          .get("/api/runtime-engine/{processDefinition}/13/{instanceId}")
        .then()
          .log().ifError()
          .statusCode(200)
          .body("state", equalTo("ACTIVE"))
          .body("process", startsWith("AccountReconcile:1"));

    // check if we can find it using the generic apis
    RestAssured.given(spec)
          .pathParam("processDefinition", "AccountReconcile")
        .when()
          .get("/api/runtime-engine/{processDefinition}")
        .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("id", hasItem(String.valueOf(id)));

    // cancel the process
    RestAssured.given(spec)
          .pathParam("processDefinition", "AccountReconcile")
          .pathParam("businessKey", "15")
          .pathParam("instanceId", id)
        .when()
          .delete("/api/runtime-engine/{processDefinition}/{businessKey}/{instanceId}")
        .then()
          .log().ifError()
          .statusCode(204);

    // verify no processes exist for the account reconcile
    RestAssured.given(spec)
          .pathParam("processDefinition", "AccountReconcile")
        .when()
          .get("/api/runtime-engine/{processDefinition}")
        .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("$", hasSize(0));
  }
}
