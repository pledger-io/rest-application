package com.jongsoft.finance.exporter;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Export")
public class ExportTest extends RestTestSetup {

    @Test
    @DisplayName("Export profile with default accounts and categories")
    void exportProfile(PledgerContext context, PledgerRequests requests) throws InterruptedException {
        context.withUser("export-test@account.local")
              .withStorage()
              .withBankAccount("Savings account", "EUR", "savings")
              .withBankAccount("Credit card account", "EUR", "credit_card")
              .withBankAccount("Checking account", "EUR", "default")
              .withDebtor("Employer", "EUR")
              .withCreditor("Netflix", "EUR")
              .withContract("Netflix", "Monthly subscription", LocalDate.now(), LocalDate.now().plusYears(1))
              .withCategory("Grocery")
              .withCategory("Shopping")
              .withCategory("Transportation")
              .withSchedule("Checking account", "Netflix", "Netflix monthly", 19.95, LocalDate.now().minusYears(1), LocalDate.now().plusYears(1))
              .withTag("Vacation 2023")
              .withTag("Vacation 2024");

        requests.createExport()
              .statusCode(200)
              .body("accounts", hasSize(5))
              .body("accounts.name", hasItems("Checking account", "Credit card account", "Savings account", "Employer", "Netflix"))
              .body("categories", hasSize(3))
              .body("categories.name", hasItems("Grocery", "Shopping", "Transportation"))
              .body("contract", hasSize(1))
              .body("contract.name", hasItems("Monthly subscription"))
              .body("schedules", hasSize(1))
              .body("schedules.name", hasItems("Netflix monthly"))
              .body("schedules.activeBetween.startDate", hasItems(LocalDate.now().minusYears(1).toString()))
              .body("schedules.activeBetween.endDate", hasItems(LocalDate.now().plusYears(1).toString()))
              .body("tags", hasSize(2))
              .body("tags", hasItems("Vacation 2023", "Vacation 2024"));
    }
}
