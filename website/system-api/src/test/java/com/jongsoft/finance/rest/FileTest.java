package com.jongsoft.finance.rest;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"jpa", "h2", "test"})
public class FileTest {

  @Inject
  private UserProvider userProvider;
  @Inject
  private ApplicationEventPublisher eventPublisher;

  @Replaces
  @MockBean
  public AuthenticationFacade authenticationFacade() {
    var mockedFacade = mock(AuthenticationFacade.class);
    when(mockedFacade.authenticated()).thenReturn("test@account.local");
    return mockedFacade;
  }

  @BeforeEach
  void setupUserAccount() {
    new EventBus(eventPublisher);
    userProvider.lookup(new UserIdentifier("test@account.local"))
        .ifNotPresent(() -> new UserAccount("test@account.local", "test123"));
  }

  @Test
  void uploadAndCheckFile(RequestSpecification spec) throws IOException {
    var file = getClass().getResource("/logback.xml").getFile();

    var fileCode =
      given(spec)
        .multiPart("upload", new File(file))
      .when()
        .post("/api/files")
      .then()
        .log().ifError()
        .statusCode(201)
        .body("fileCode", notNullValue())
        .extract()
        .jsonPath()
        .getString("fileCode");

    var fileContent = given(spec)
        .pathParam("fileCode", fileCode)
      .when()
        .get("/api/files/{fileCode}")
      .then()
        .statusCode(200)
        .extract()
        .asByteArray();

    var expected = Files.readAllBytes(new File(file).toPath());
    assertThat(fileContent)
        .containsExactly(expected);

    given(spec)
        .pathParam("fileCode", fileCode)
        .delete("/api/files/{fileCode}")
      .then()
        .statusCode(204);

    given(spec)
        .pathParam("fileCode", fileCode)
      .when()
        .get("/api/files/{fileCode}")
      .then()
        .statusCode(500);
  }
}
