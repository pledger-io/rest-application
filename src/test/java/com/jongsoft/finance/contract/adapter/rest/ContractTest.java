package com.jongsoft.finance.contract.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Contracts")
public class ContractTest extends RestTestSetup {

    @Test
    @DisplayName("Create, update, fetch and delete a contract")
    void createContract(PledgerContext context, PledgerRequests requests) {
        context.withUser("contract-create@account.local")
              .withCreditor("Netflix", "EUR");
        requests.authenticate("contract-create@account.local");

        var accountId = requests.searchBankAccounts(0,1, List.of("creditor"), "Netflix")
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

    @Test
    @DisplayName("Search for contracts")
    void searchForContract(PledgerContext context, PledgerRequests requests) {
        context.withUser("contract-search@account.local")
              .withCreditor("Amazon", "EUR")
              .withCreditor("Netflix", "EUR")
              .withContract("Netflix", "Monthly subscription", LocalDate.now().minusDays(1), LocalDate.now().plusYears(1))
              .withContract("Amazon", "Amazon Prime", LocalDate.now().minusYears(1), LocalDate.now().minusDays(1));
        requests.authenticate("contract-search@account.local");

        requests.searchContracts("monthly", null)
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].name", equalTo("Monthly subscription"));

        requests.searchContracts(null, "INACTIVE")
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].name", equalTo("Amazon Prime"));

        requests.searchContracts(null, "ACTIVE")
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].name", equalTo("Monthly subscription"));
    }
}
