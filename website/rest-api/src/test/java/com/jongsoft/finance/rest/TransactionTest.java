package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerRequests;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class TransactionTest {

    @Test
    void createTransactionSchedule(PledgerContext context, PledgerRequests requests) {
        context.withUser("transaction-schedule-create@account.local")
              .withBankAccount("Checking", "EUR", "default")
              .withCreditor("Netflix", "EUR");

        var sourceId = requests.searchBankAccounts(0, 1, List.of(), "checking")
              .extract().jsonPath().getLong("content[0].id");
        var destinationId = requests.searchBankAccounts(0, 1, List.of(), "netfl")
              .extract().jsonPath().getLong("content[0].id");

        var scheduleId = requests.createScheduleMonthly(sourceId, destinationId, "Netflix Monthly Payment", 19.99)
              .statusCode(201)
              .body("id", notNullValue())
              .body("name", equalTo("Netflix Monthly Payment"))
              .body("amount", equalTo(19.99F))
              .extract().jsonPath().getLong("id");

        requests.patchScheduleDateRange(scheduleId, LocalDate.now(), LocalDate.now().plusMonths(12))
              .statusCode(200)
              .body("activeBetween.startDate", equalTo(LocalDate.now().toString()))
              .body("activeBetween.endDate", equalTo(LocalDate.now().plusMonths(12).toString()));

        requests.fetchSchedule(scheduleId)
              .statusCode(200)
              .body("name", equalTo("Netflix Monthly Payment"))
              .body("amount", equalTo(19.99F))
              .body("transferBetween.source.id", equalTo((int)sourceId))
              .body("transferBetween.source.name", equalTo("Checking"))
              .body("transferBetween.destination.id", equalTo((int)destinationId))
              .body("transferBetween.destination.name", equalTo("Netflix"))
              .body("activeBetween.startDate", equalTo(LocalDate.now().toString()))
              .body("activeBetween.endDate", equalTo(LocalDate.now().plusMonths(12).toString()));

        requests.deleteSchedule(scheduleId)
              .statusCode(204);

        requests.fetchSchedule(scheduleId)
              .statusCode(410);
    }

    @Test
    void searchForSchedule(PledgerContext context, PledgerRequests requests) {
        context.withUser("transaction-schedule-search@account.local")
              .withBankAccount("Checking", "EUR", "default")
              .withCreditor("Netflix", "EUR")
              .withSchedule("Checking", "Netflix", "Monthly payment", 19.99, LocalDate.now(), LocalDate.now().plusMonths(12));

        requests.searchSchedules(null, null)
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].name", equalTo("Monthly payment"))
              .body("[0].amount", equalTo(19.99F));
    }
}
