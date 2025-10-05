package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerRequests;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class ExportTest {

    @Test
    void exportProfile(PledgerContext context, PledgerRequests requests) {
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
              .body("tags", hasSize(2))
              .body("tags", hasItems("Vacation 2023", "Vacation 2024"));
    }
}
