package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

@DisplayName("Scheduled transaction resource")
class ScheduledTransactionResourceTest extends TestSetup {

    @Inject
    private AccountProvider accountProvider;
    @Inject
    private TransactionScheduleProvider transactionScheduleProvider;
    private ScheduledTransaction scheduledTransaction;

    @Replaces
    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Replaces
    @MockBean
    TransactionScheduleProvider transactionScheduleProvider() {
        return Mockito.mock(TransactionScheduleProvider.class);
    }

    @BeforeEach
    void setup() {
        scheduledTransaction = Mockito.spy(ScheduledTransaction.builder()
                .id(1L)
                .name("Monthly gym membership")
                .amount(22.66)
                .schedule(new ScheduleValue(Periodicity.WEEKS, 4))
                .description("Gym membership")
                .start(LocalDate.of(2019, 1, 1))
                .end(LocalDate.of(2021, 1, 1))
                .source(Account.builder()
                        .id(1L)
                        .type("checking")
                        .name("My account")
                        .currency("EUR")
                        .build())
                .destination(Account.builder().id(2L).type("creditor").currency("EUR").name("Gym").build())
                .build());

        Mockito.when(accountProvider.lookup(Mockito.anyLong())).thenReturn(Control.Option());
        Mockito.when(transactionScheduleProvider.lookup()).thenReturn(Collections.List(scheduledTransaction));
    }

    @Test
    @DisplayName("List all available transaction schedules")
    void list(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .get("/api/schedule/transaction")
            .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(1))
                .body("[0].name", Matchers.equalTo("Monthly gym membership"))
                .body("[0].description", Matchers.equalTo("Gym membership"))
                .body("[0].range.start", Matchers.equalTo("2019-01-01"))
                .body("[0].range.end", Matchers.equalTo("2021-01-01"));
        // @formatter:on
    }

    @Test
    @DisplayName("Create a new transaction schedule")
    void create(RequestSpecification spec) {
        var destinationAccount = Account.builder().id(1L).build();
        var sourceAccount = Mockito.spy(Account.builder().id(2L).build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(Account.builder().id(1L).build()));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(sourceAccount));
        Mockito.when(transactionScheduleProvider.lookup())
                .thenReturn(Collections.List(scheduledTransaction, ScheduledTransaction.builder()
                        .name("Sample schedule")
                        .build()));
        Mockito.when(transactionScheduleProvider.lookup())
                .thenReturn(Collections.List(
                                scheduledTransaction,
                                ScheduledTransaction.builder()
                                        .id(2L)
                                        .amount(22.2)
                                        .schedule(new ScheduleValue(Periodicity.WEEKS, 1))
                                        .destination(destinationAccount)
                                        .source(sourceAccount)
                                        .name("Sample schedule")
                                        .build()));

        // @formatter:off
        spec.given()
                .body("""
                        {
                            "amount": 22.2,
                            "name": "Sample schedule",
                            "schedule": {
                                "periodicity": "WEEKS",
                                "value": 1
                            },
                            "destination": {
                                "id": 1
                            },
                            "source": {
                                "id": 2
                            }
                        }
                        """)
            .when()
                .put("/api/schedule/transaction")
            .then()
                .statusCode(201)
                .body("name", Matchers.equalTo("Sample schedule"));
        // @formatter:on
        Mockito.verify(sourceAccount).createSchedule(
                "Sample schedule",
                new ScheduleValue(Periodicity.WEEKS, 1),
                destinationAccount,
                22.2);
    }

    @Test
    @DisplayName("Get a schedule by id")
    void get(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .get("/api/schedule/transaction/1")
            .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("Monthly gym membership"))
                .body("description", Matchers.equalTo("Gym membership"))
                .body("range.start", Matchers.equalTo("2019-01-01"))
                .body("range.end", Matchers.equalTo("2021-01-01"));
        // @formatter:on
    }

    @Test
    @DisplayName("Patch a schedule")
    void patch(RequestSpecification spec) {
        // @formatter:off
        spec.given()
                .body("""
                        {
                            "description": "Updated description",
                            "name": "New name",
                            "range": {
                                "start": "2021-01-01",
                                "end": "2022-01-01"
                            }
                        }
                        """)
            .when()
                .patch("/api/schedule/transaction/1")
            .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("New name"))
                .body("description", Matchers.equalTo("Updated description"))
                .body("range.start", Matchers.equalTo("2021-01-01"))
                .body("range.end", Matchers.equalTo("2022-01-01"));
        // @formatter:on

        Mockito.verify(scheduledTransaction).describe("New name", "Updated description");
        Mockito.verify(scheduledTransaction).limit(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1));
    }

    @Test
    @DisplayName("Remove a schedule by id")
    void remove(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .delete("/api/schedule/transaction/1")
            .then()
                .statusCode(204);
        // @formatter:on

        Mockito.verify(scheduledTransaction).terminate();
    }

    @Test
    @DisplayName("Remove a schedule by id - not found")
    void remove_notFound(RequestSpecification spec) {
        // @formatter:off
        spec.when()
                .delete("/api/schedule/transaction/2")
            .then()
                .statusCode(404)
                .body("message", Matchers.equalTo("No scheduled transaction found with id 2"));
        // @formatter:on
    }
}
