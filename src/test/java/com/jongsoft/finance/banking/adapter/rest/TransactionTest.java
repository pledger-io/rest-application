package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Transactions")
public class TransactionTest extends RestTestSetup {

    @Test
    @DisplayName("Create a transaction schedule, update it and search for it")
    void createTransactionSchedule(PledgerContext context, PledgerRequests requests) {
        context.withUser("transaction-schedule-create@account.local")
              .withBankAccount("Checking", "EUR", "default")
              .withCreditor("Netflix", "EUR");

        var sourceId = requests.searchBankAccounts(0, 1, List.of(), "checking")
              .extract().jsonPath().getLong("content[0].id");
        var destinationId = requests.searchBankAccounts(0, 1, List.of("creditor"), "netfl")
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
    @DisplayName("Search for a transaction schedule")
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

    @Test
    @DisplayName("Create a transaction, update it and search for it")
    void createTransaction(PledgerContext context, PledgerRequests requests) {
        context.withUser("transaction-create@account.local")
              .withBankAccount("Checking", "EUR", "default")
              .withCreditor("Netflix", "EUR");

        var sourceId = requests.searchBankAccounts(0, 1, List.of(), "checking")
              .extract().jsonPath().getLong("content[0].id");
        var destinationId = requests.searchBankAccounts(0, 1, List.of("creditor"), "netfl")
              .body("content[0].id", notNullValue())
              .extract().jsonPath().getLong("content[0].id");

        var id = requests.createTransaction(sourceId, destinationId, 19.99, "EUR", LocalDate.now(), "Monthly payment")
              .statusCode(201)
              .body("id", notNullValue())
              .body("amount", equalTo(19.99F))
              .body("currency", equalTo("EUR"))
              .body("dates.transaction", equalTo(LocalDate.now().toString()))
              .body("description", equalTo("Monthly payment"))
              .extract().jsonPath().getLong("id");

        requests.fetchTransaction(id)
              .statusCode(200);
        requests.fetchTransaction(9000)
              .statusCode(404);

        context.withTag("streaming")
              .withCategory("Streaming Services");

        var catId = requests.searchCategories(0, 2, "stream")
              .body("content[0].id", notNullValue())
              .extract().jsonPath().getLong("content[0].id");

        requests.updateTransaction(id, sourceId, destinationId, "Netflix payment", 19.95, LocalDate.now(), catId, null, null, "streaming")
              .statusCode(200)
              .body("amount", equalTo(19.95F))
              .body("currency", equalTo("EUR"))
              .body("dates.transaction", equalTo(LocalDate.now().toString()))
              .body("description", equalTo("Netflix payment"))
              .body("metadata.category", equalTo("Streaming Services"))
              .body("metadata.tags", hasItem("streaming"));

        requests.searchTransactionsForAccounts(0, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(1), List.of(sourceId))
              .statusCode(200)
              .body("content", hasSize(1))
              .body("content[0].amount", equalTo(19.95F));

        requests.deleteTransaction(id)
              .statusCode(204);

        requests.fetchTransaction(id)
              .statusCode(410);
    }
}
