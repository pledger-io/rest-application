package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.messaging.EventBus;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpHeaders;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.generator.AccessRefreshTokenGenerator;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

@MicronautTest(environments = {"jpa", "h2", "openid", "test", "security"})
public class SecurityTest {

  @Inject
  private AccessRefreshTokenGenerator accessRefreshTokenGenerator;

  @MockBean(MailDaemon.class)
  MailDaemon mailDaemon() {
    return mock(MailDaemon.class);
  }

  @BeforeEach
  void setup(ApplicationEventPublisher publisher) {
    new EventBus(publisher);
  }

  @Test
  void checkTheOpenIdConfiguration(RequestSpecification spec) {
    spec.given()
        .get("/.well-known/openid-connect")
        .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("authority", equalTo("http://authority.tst.local"))
          .body("client-id", equalTo("my-client-id"))
          .body("client-secret", equalTo("super-secret-password"));
  }

  @Test
  void registerNewAccount(RequestSpecification spec) {
    spec.given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "username", "test@account.local",
            "password", "test123"))
        .when()
          .post("/v2/api/user-account")
        .then()
          .log().ifValidationFails()
          .statusCode(204);

    // fetch profile
    given(spec)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken())
        .pathParam("user-account", "test@account.local")
        .get("/v2/api/user-account/{user-account}")
        .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("theme", equalTo("light"))
          .body("currency", equalTo("EUR"))
          .body("mfa", equalTo(false));

      given(spec)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken())
          .pathParam("user-account", "test@account.local")
          .body(Map.of("description", "Long session", "expires", "2028-02-01"))
          .post("/v2/api/user-account/{user-account}/sessions")
          .then()
          .log()
          .ifValidationFails()
          .statusCode(201)
          .body("description", equalTo("Pledger.io Web login"))
          .body("valid.startDate", equalTo(LocalDate.now().toString()))
          .body("valid.endDate", equalTo("2028-01-31"));

      given(spec)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken())
          .pathParam("user-account", "test@account.local")
          .get("/v2/api/user-account/{user-account}/sessions")
          .then()
          .log().ifValidationFails()
          .statusCode(200)
          .body("[0].description", equalTo("Pledger.io Web login"))
          .body("[0].valid.startDate", equalTo(LocalDate.now().toString()))
          .body("[0].valid.endDate", equalTo("2028-01-31"));

    // change currency to GBP
    given(spec)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken())
        .pathParam("user-account", "test@account.local")
        .body(Map.of(
            "theme", "dark",
            "currency", "GBP"))
        .patch("/v2/api/user-account/{user-account}")
        .then()
          .log().ifValidationFails(LogDetail.ALL)
          .statusCode(200)
          .body("theme", equalTo("dark"))
          .body("currency", equalTo("GBP"))
          .body("mfa", equalTo(false));
  }

  private String getToken() {
    return accessRefreshTokenGenerator.generate(Authentication.build("test@account.local"))
        .get()
        .getAccessToken();
  }
}
