package com.jongsoft.finance.rest;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
public class RuleTest {

    @Inject private ApplicationEventPublisher eventPublisher;
    @Inject private UserProvider userProvider;

    @MockBean
    @Replaces(CurrentUserProvider.class)
    CurrentUserProvider currentUserProvider() {
        var currentUserProvider = mock(CurrentUserProvider.class);
        when(currentUserProvider.currentUser())
              .thenAnswer(_ -> userProvider.lookup(new UserIdentifier("test-account@local")).get());
        return currentUserProvider;
    }

    @MockBean
    @Replaces(AuthenticationFacade.class)
    AuthenticationFacade authenticationFacade() {
        var mockedFacade = mock(AuthenticationFacade.class);
        when(mockedFacade.authenticated()).thenReturn("test-account@local");
        return mockedFacade;
    }

    @BeforeEach
    void setup() {
        new EventBus(eventPublisher);
        userProvider.lookup(new UserIdentifier("test-account@local"))
              .ifNotPresent(() -> new UserAccount("test-account@local", "test123"));
    }

    @Test
    void createRuleInGroup(RequestSpecification spec) {
        given(spec)
            .contentType("application/json")
            .body(Map.of("name", "Shops"))
          .when()
            .post("/v2/api/transaction-rules")
          .then()
            .statusCode(204);

        Long id = given(spec)
              .contentType("application/json")
              .pathParam("group", "Shops")
              .body(Map.of(
                    "name", "Grocery shop",
                    "active", true,
                    "restrictive", false,
                    "changes", List.of(
                          Map.of("column", "SOURCE_ACCOUNT", "value","1")
                    ),
                    "conditions", List.of(
                          Map.of("column", "SOURCE_ACCOUNT", "operation","STARTS_WITH", "value", "wall")
                    )
                    ))
          .when()
              .post("/v2/api/transaction-rules/{group}")
          .then()
              .log().ifValidationFails()
              .statusCode(201)
              .body("name", equalTo("Grocery shop"))
              .body("active", equalTo(true))
              .body("restrictive", equalTo(false))
              .body("changes", hasSize(1))
              .body("changes[0].field", equalTo("SOURCE_ACCOUNT"))
              .body("conditions", hasSize(1))
              .extract().jsonPath().getLong("id");

        given(spec)
              .contentType("application/json")
              .pathParam("group", "Shops")
              .pathParam("id", id)
          .when()
              .delete("/v2/api/transaction-rules/{group}/{id}")
          .then()
              .statusCode(204);
    }
}
