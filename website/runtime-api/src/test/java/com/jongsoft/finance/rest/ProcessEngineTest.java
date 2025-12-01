package com.jongsoft.finance.rest;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.StartProcessCommand;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.UserProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
public class ProcessEngineTest {

    @Inject
    private UserProvider userProvider;
    @Inject
    private AccountProvider accountProvider;
    @Inject
    private ApplicationEventPublisher eventPublisher;
    private long accountId;

    @MockBean(AuthenticationFacade.class)
    AuthenticationFacade authenticationFacade() {
        var mockedFacade = mock(AuthenticationFacade.class);
        when(mockedFacade.authenticated()).thenReturn("reconcile@account.local");
        return mockedFacade;
    }

    @MockBean
    StorageService storageService() {
        return mock(StorageService.class);
    }

    @MockBean
    MailDaemon mailDaemon() {
        return mock(MailDaemon.class);
    }

    @MockBean(AuthenticationFacade.class)
    CurrentUserProvider currentUserProvider(UserProvider userProvider) {
        var mockedFacade = mock(CurrentUserProvider.class);
        when(mockedFacade.currentUser())
              .thenAnswer(_ -> userProvider.lookup(new UserIdentifier("reconcile@account.local")).get());
        return mockedFacade;
    }

    @Bean
    FinTrack application() {
        return new FinTrack(new Encoder() {
            @Override
            public String encrypt(String value) {
                return value;
            }

            @Override
            public boolean matches(String encoded, String value) {
                return Objects.equals(encoded, value);
            }
        });
    }

    @BeforeEach
    void setup() {
        new EventBus(eventPublisher);
        userProvider.lookup(new UserIdentifier("reconcile@account.local"))
              .ifNotPresent(() -> {
                  StartProcessCommand.startProcess(
                        "RegisterUserAccount",
                        Map.of("username", new UserIdentifier("reconcile@account.local"), "passwordHash", "test123")
                  );

                  userProvider.lookup(new UserIdentifier("reconcile@account.local"))
                        .get().createAccount("Checking test account", "EUR", "default");
                  accountId = accountProvider.lookup("Checking test account").get().getId();
              });
    }

    @Test
    @DisplayName("Start an account reconcile, fetch it and cancel the process")
    void performAccountReconcileProcess(RequestSpecification spec) {
        // Create the account reconcile process
        var year2024 = balanceOutYear(spec, 2024, 10.0, 100.0, false);

        // verify the process exists in the system
        RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
              .pathParam("instanceId", year2024)
          .when()
              .get("/v2/api/runtime-engine/{processDefinition}/13/{instanceId}")
          .then()
              .log().ifError()
              .statusCode(200)
              .body("state", equalTo("ACTIVE"))
              .body("process", startsWith("AccountReconcile:1"));

        checkOpenBalancing(spec, String.valueOf(year2024));

        var taskId = checkOpenBalanceTask(spec, year2024, true);
        checkOpenTaskVariable(spec, year2024, taskId, 2024, 100, 10, 0);

        balanceOutYear(spec, 2023, 0.0, 10.0, true);

        checkOpenBalancing(spec, String.valueOf(year2024));

        RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
              .pathParam("instanceId", year2024)
              .pathParam("taskId", taskId)
          .when()
              .delete("/v2/api/runtime-engine/{processDefinition}/13/{instanceId}/tasks/{taskId}")
          .then()
              .log().ifValidationFails()
              .statusCode(204);

        checkOpenBalanceTask(spec, year2024, false);

        // verify no processes exist for the account reconcile
        RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
              .when()
              .get("/v2/api/runtime-engine/{processDefinition}")
              .then()
              .log().ifValidationFails()
              .statusCode(200)
              .body("$", hasSize(0));
    }

    private void checkOpenBalancing(RequestSpecification spec, String...processIds) {
        RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
          .when()
              .get("/v2/api/runtime-engine/{processDefinition}")
          .then()
              .log().ifValidationFails()
              .statusCode(200)
              .body("id", hasItems(processIds));
    }

    private long checkOpenBalanceTask(RequestSpecification spec, int processId, boolean expectedTask) {
        var taskResponse = RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
              .pathParam("instanceId", processId)
          .when()
              .get("/v2/api/runtime-engine/{processDefinition}/13/{instanceId}/tasks")
          .then()
              .log().ifValidationFails()
              .statusCode(200);

        if (expectedTask) {
            return taskResponse.body("id", hasItem(notNullValue()))
                  .body("name", hasItem(equalTo("Start differs warning")))
                  .body("definition", hasItem(equalTo("task_reconcile_before")))
                  .extract().jsonPath().getLong("[0].id");
        } else {
            taskResponse.body("$", hasSize(0));
        }

        return -1;
    }

    private void checkOpenTaskVariable(RequestSpecification spec, int processId, long taskId, int year, double endBalance, double startBalance, double computedStartBalance) {
        RestAssured.given(spec)
              .pathParam("processDefinition", "AccountReconcile")
              .pathParam("instanceId", processId)
              .pathParam("taskId", taskId)
          .when()
              .get("/v2/api/runtime-engine/{processDefinition}/13/{instanceId}/tasks/{taskId}/variables")
          .then()
              .log().ifValidationFails()
              .statusCode(200)
              .body("variables.accountId.value", equalTo((int)accountId))
              .body("variables.endDate.value", equalTo(year + "-01-01"))
              .body("variables.endBalance.value", equalTo((float)endBalance))
              .body("variables.computedStartBalance.value", equalTo((int)computedStartBalance))
              .body("variables.openBalance.value", equalTo((float)startBalance));
    }

    private int balanceOutYear(RequestSpecification spec, int year, double startBalance, double endBalance, boolean expectComplete) {
        return RestAssured.given(spec)
              .contentType(ContentType.JSON)
              .body(Map.of(
                    "accountId", accountId,
                    "startDate", year + "-01-01",
                    "endDate", year + "-12-31",
                    "openBalance", startBalance,
                    "endBalance", endBalance))
          .when()
              .pathParam("processDefinition", "AccountReconcile")
              .post("/v2/api/runtime-engine/{processDefinition}")
          .then()
              .log().ifError()
              .statusCode(201)
              .body("state", equalTo(expectComplete ? "COMPLETED" : "ACTIVE"))
              .body("process", startsWith("AccountReconcile:1"))
              .extract().jsonPath().getInt("id");
    }
}
