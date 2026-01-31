package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.UserIdentifier;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Regression - File Upload")
@MicronautTest(environments = {"jpa", "h2", "test", "test-jpa"})
public class FileTest {

  @Inject
  private UserProvider userProvider;

  @Replaces
  @MockBean
  public AuthenticationFacade authenticationFacade() {
    var mockedFacade = mock(AuthenticationFacade.class);
    when(mockedFacade.authenticated()).thenReturn("test@account.local");
    return mockedFacade;
  }

  @BeforeEach
  void setupUserAccount() {
    userProvider.lookup(new UserIdentifier("test@account.local"))
        .ifNotPresent(() -> UserAccount.create("test@account.local", "test123"));
  }

  @Test
  @DisplayName("Upload a file and check it can be retrieved")
  void uploadAndCheckFile(RequestSpecification spec) throws IOException {
    var file = getClass().getResource("/logback.xml").getFile();

    var fileCode =
      given(spec)
        .multiPart("upload", new File(file))
      .when()
        .post("/v2/api/files")
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
        .get("/v2/api/files/{fileCode}")
      .then()
        .statusCode(200)
        .extract()
        .asByteArray();

    var expected = Files.readAllBytes(new File(file).toPath());
    assertThat(fileContent)
        .containsExactly(expected);

    given(spec)
        .pathParam("fileCode", fileCode)
        .delete("/v2/api/files/{fileCode}")
      .then()
        .statusCode(204);

    given(spec)
        .log().ifValidationFails()
        .pathParam("fileCode", fileCode)
      .when()
        .get("/v2/api/files/{fileCode}")
      .then()
        .log().ifValidationFails()
        .statusCode(404);
  }
}
