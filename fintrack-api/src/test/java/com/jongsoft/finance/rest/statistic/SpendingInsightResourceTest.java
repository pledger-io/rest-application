package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.domain.insight.*;
import com.jongsoft.finance.providers.SpendingInsightProvider;
import com.jongsoft.finance.providers.SpendingPatternProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@DisplayName("Statistic: Spending Insights and Patterns")
class SpendingInsightResourceTest extends TestSetup {

  @Inject
  private SpendingInsightProvider spendingInsightProvider;

  @Inject
  private SpendingPatternProvider spendingPatternProvider;

  @Replaces
  @MockBean
  SpendingInsightProvider spendingInsightProvider() {
    return Mockito.mock(SpendingInsightProvider.class);
  }

  @Replaces
  @MockBean
  SpendingPatternProvider spendingPatternProvider() {
    return Mockito.mock(SpendingPatternProvider.class);
  }

  @BeforeEach
  void setup() {
    Mockito.when(spendingInsightProvider.lookup(Mockito.any(YearMonth.class)))
        .thenReturn(Collections.List());
    Mockito.when(spendingPatternProvider.lookup(Mockito.any(YearMonth.class)))
        .thenReturn(Collections.List());
  }

  @Test
  @DisplayName("Get spending insights")
  void getInsights(RequestSpecification spec) {
    // Create a mock insight
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("testKey", "testValue");

    SpendingInsight mockInsight = SpendingInsight.builder()
        .type(InsightType.UNUSUAL_AMOUNT)
        .category("Groceries")
        .severity(Severity.WARNING)
        .score(0.85)
        .transactionId(123L)
        .detectedDate(LocalDate.of(2023, 1, 15))
        .message("Unusual spending amount detected")
        .metadata(metadata)
        .build();

    // Setup the mock to return our test insight
    Mockito.when(spendingInsightProvider.lookup(YearMonth.of(2023, 1)))
        .thenReturn(Collections.List(mockInsight));

    // @formatter:off
    spec
      .given()
      .when()
        .get("/api/statistics/spending/insights?year=2023&month=1")
      .then()
        .statusCode(200)
        .body("size()", Matchers.equalTo(1))
        .body("[0].type", Matchers.equalTo("UNUSUAL_AMOUNT"))
        .body("[0].category", Matchers.equalTo("Groceries"))
        .body("[0].severity", Matchers.equalTo("WARNING"))
        .body("[0].score", Matchers.equalTo(0.85f))
        .body("[0].transactionId", Matchers.equalTo(123))
        .body("[0].message", Matchers.equalTo("Unusual spending amount detected"));
    // @formatter:on

    Mockito.verify(spendingInsightProvider).lookup(YearMonth.of(2023, 1));
  }

  @Test
  @DisplayName("Get spending patterns")
  void getPatterns(RequestSpecification spec) {
    // Create a mock pattern
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("testKey", "testValue");

    SpendingPattern mockPattern = SpendingPattern.builder()
        .type(PatternType.RECURRING_MONTHLY)
        .category("Utilities")
        .confidence(0.95)
        .detectedDate(LocalDate.of(2023, 1, 15))
        .metadata(metadata)
        .build();

    // Setup the mock to return our test pattern
    Mockito.when(spendingPatternProvider.lookup(YearMonth.of(2023, 1)))
        .thenReturn(Collections.List(mockPattern));

    // @formatter:off
    spec
      .given()
      .when()
          .get("/api/statistics/spending/patterns?year=2023&month=1")
      .then()
          .statusCode(200)
          .body("size()", Matchers.equalTo(1))
          .body("[0].type", Matchers.equalTo("RECURRING_MONTHLY"))
          .body("[0].category", Matchers.equalTo("Utilities"))
          .body("[0].confidence", Matchers.equalTo(0.95f))
          .body("[0].detectedDate", Matchers.equalTo("2023-01-15"));
    // @formatter:on

    Mockito.verify(spendingPatternProvider).lookup(YearMonth.of(2023, 1));
  }

  @Test
  @DisplayName("Get empty insights")
  void getEmptyInsights(RequestSpecification spec) {
    // @formatter:off
    spec
      .given()
      .when()
        .get("/api/statistics/spending/insights?year=2023&month=2")
      .then()
        .statusCode(200)
        .body("size()", Matchers.equalTo(0));
    // @formatter:on

    Mockito.verify(spendingInsightProvider).lookup(YearMonth.of(2023, 2));
  }

  @Test
  @DisplayName("Get empty patterns")
  void getEmptyPatterns(RequestSpecification spec) {
    // @formatter:off
    spec
      .given()
      .when()
        .get("/api/statistics/spending/patterns?year=2023&month=2")
      .then()
        .statusCode(200)
        .body("size()", Matchers.equalTo(0));
    // @formatter:on

    Mockito.verify(spendingPatternProvider).lookup(YearMonth.of(2023, 2));
  }
}
