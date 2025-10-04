package com.jongsoft.finance.rest;

import com.jongsoft.finance.rest.extension.PledgerContext;
import com.jongsoft.finance.rest.extension.PledgerRequests;
import com.jongsoft.finance.rest.extension.PledgerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@MicronautTest(environments = {"jpa", "h2", "test"}, transactional = false)
@PledgerTest
public class ContractTest {

    @Test
    void createContract(PledgerContext context, PledgerRequests requests) {
        context.withUser("contract-create@account.local")
              .withCreditor("Netflix", "EUR");

        var accountId = requests.searchBankAccounts(0,1, List.of(), "Netflix")
              .extract().jsonPath().getLong("content[0].id");

        var contractId = requests.createContract(accountId, "Netflix Monthly", "Monthly subscription", LocalDate.now(), LocalDate.now().plusYears(1))
              .statusCode(201)
              .body("id", notNullValue())
              .body("name", equalTo("Netflix Monthly"))
              .body("description", equalTo("Monthly subscription"))
              .body("company.id", equalTo((int)accountId))
              .body("company.name", equalTo("Netflix"))
              .body("company.type", equalTo("creditor"))
              .body("terminated", equalTo(false))
              .body("notification", equalTo(false))
              .body("start", equalTo(LocalDate.now().toString()))
              .body("end", equalTo(LocalDate.now().plusYears(1).toString()))
              .extract().jsonPath().getLong("id");

        requests.updateContract(contractId, "Netflix Monthly", "Monthly netflix", LocalDate.now(), LocalDate.now().plusYears(2))
              .statusCode(200)
              .body("name", equalTo("Netflix Monthly"))
              .body("description", equalTo("Monthly netflix"))
              .body("company.id", equalTo((int)accountId))
              .body("start", equalTo(LocalDate.now().toString()))
              .body("end", equalTo(LocalDate.now().plusYears(2).toString()));

        requests.fetchContract(contractId)
              .statusCode(200)
              .body("name", equalTo("Netflix Monthly"))
              .body("description", equalTo("Monthly netflix"))
              .body("company.id", equalTo((int)accountId));

        requests.warnBeforeContractExpires(contractId)
              .statusCode(204);

        requests.warnBeforeContractExpires(contractId)
              .statusCode(400)
              .body("message", equalTo("Warning already scheduled for contract"));

        requests.updateContract(contractId, "Netflix Monthly", "Monthly netflix", LocalDate.now().minusYears(1), LocalDate.now().minusDays(1))
              .statusCode(200)
              .body("name", equalTo("Netflix Monthly"))
              .body("description", equalTo("Monthly netflix"))
              .body("company.id", equalTo((int)accountId))
              .body("start", equalTo(LocalDate.now().minusYears(1).toString()))
              .body("end", equalTo(LocalDate.now().minusDays(1).toString()));

        requests.deleteContract(contractId)
              .statusCode(204);
    }
}
