package com.jongsoft.finance.core.adapter.rest;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@DisplayName("Regression - I18n")
@MicronautTest(environments = {"jpa", "h2", "test", "test-jpa"})
public class I18nTest {

  @Test
  @DisplayName("Fetch translations for Dutch")
  void fetchDutchTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "nl")
        .when()
          .get("/v2/api/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Bewerken"));
  }

  @Test
  @DisplayName("Fetch translations for English")
  void fetchEnglishTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "en")
        .when()
          .get("/v2/api/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Edit"));
  }

  @Test
  @DisplayName("Fetch translations for German")
  void fetchGermanTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "de")
        .when()
          .get("/v2/api/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Bearbeiten"));
  }
}
