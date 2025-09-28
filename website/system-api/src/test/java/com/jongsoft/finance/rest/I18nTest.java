package com.jongsoft.finance.rest;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@MicronautTest(environments = {"jpa", "h2", "test"})
public class I18nTest {

  @Test
  void fetchDutchTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "nl")
        .when()
          .get("/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Bewerken"));
  }

  @Test
  void fetchEnglishTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "en")
        .when()
          .get("/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Edit"));
  }

  @Test
  void fetchGermanTranslations(RequestSpecification spec) {
    spec.given()
          .pathParam("languageCode", "de")
        .when()
          .get("/i18n/{languageCode}")
        .then()
          .statusCode(200)
          .body("'common.action.edit'", equalTo("Bearbeiten"));
  }
}
