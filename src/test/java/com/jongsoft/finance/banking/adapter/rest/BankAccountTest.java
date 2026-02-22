package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Bank accounts")
public class BankAccountTest extends RestTestSetup {

    @Test
    @DisplayName("List all account types")
    void listAllAccountTypes(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-types@account.local");
        requests.authenticate("bank-account-types@account.local");

        requests.fetchAccountTypes()
              .statusCode(200)
              .body("$", hasSize(6))
              .body("[0]", equalTo("cash"))
              .body("[1]", equalTo("credit_card"))
              .body("[2]", equalTo("default"))
              .body("[3]", equalTo("joined"))
              .body("[4]", equalTo("joined_savings"))
              .body("[5]", equalTo("savings"));
    }

    @Test
    @DisplayName("Create new bank account")
    void creatingNewBankAccount(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-create@account.local");
        requests.authenticate("bank-account-create@account.local");

        var id = requests.createBankAccount(Map.of(
                    "name", "Lidl Stores",
                    "description", "Lidl stores in Berlin",
                    "iban", "NL00ABNA0417164300",
                    "currency", "EUR",
                    "type", "creditor"))
              .statusCode(201)
              .body("id", notNullValue())
              .body("name", equalTo("Lidl Stores"))
              .body("description", equalTo("Lidl stores in Berlin"))
              .body("account.currency", equalTo("EUR"))
              .body("account.iban", equalTo("NL00ABNA0417164300"))
              .extract()
              .jsonPath().getLong("id");

       requests.updateBankAccount(id, Map.of(
                    "name", "Lidl Stores",
                    "description", "Lidl stores in Zurich",
                    "iban", "NL00ABNA0417164301",
                    "currency", "EUR",
                    "type", "creditor"))
              .statusCode(200)
              .body("id", notNullValue())
              .body("name", equalTo("Lidl Stores"))
              .body("description", equalTo("Lidl stores in Zurich"))
              .body("account.currency", equalTo("EUR"))
              .body("account.iban", equalTo("NL00ABNA0417164301"));

       requests.fetchBankAccount(id)
             .statusCode(200)
             .body("name", equalTo("Lidl Stores"))
             .body("description", equalTo("Lidl stores in Zurich"));

       requests.deleteBankAccount(id)
             .statusCode(204);

        requests.fetchBankAccount(id)
              .statusCode(410)
              .body("message", equalTo("Bank account has been removed from the system"));
    }

    @Test
    @DisplayName("Create a savings account with goals")
    void createSavingsAccountWithGoals(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-savings@account.local")
              .withBankAccount("Savings account", "EUR", "savings");
        requests.authenticate("bank-account-savings@account.local");

        var accountId = requests.searchBankAccounts(0, 1, List.of(), "Savings account")
              .statusCode(200)
              .body("content", hasSize(1))
              .extract().jsonPath().getLong("content[0].id");

        var savingGoalId = requests.createSavingGoal(accountId, "Washer", 500D, LocalDate.now().plusYears(1))
              .statusCode(201)
              .body("id", notNullValue())
              .body("name", equalTo("Washer"))
              .body("goal", equalTo(500F))
              .body("targetDate", equalTo(LocalDate.now().plusYears(1).toString()))
              .body("installments", equalTo(45.45F))
              .body("monthsLeft", equalTo(12))
              .extract().jsonPath().getInt("id");

        requests.fetchSavingGoals(accountId)
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].id", equalTo(savingGoalId))
              .body("[0].name", equalTo("Washer"));

        requests.reserveMoneyForSavingGoal(accountId, savingGoalId, 150D)
              .statusCode(204);

        requests.fetchSavingGoals(accountId)
              .statusCode(200)
              .body("$", hasSize(1))
              .body("[0].id", equalTo(savingGoalId))
              .body("[0].installments", equalTo(31.82F))
              .body("[0].reserved", equalTo(150F));

        requests.updateSavingGoal(accountId, savingGoalId, 500D, LocalDate.now().plusMonths(6))
              .statusCode(200)
              .body("id", equalTo(savingGoalId))
              .body("name", equalTo("Washer"))
              .body("goal", equalTo(500F))
              .body("targetDate", equalTo(LocalDate.now().plusMonths(6).toString()))
              .body("installments", equalTo(70F))
              .body("monthsLeft", equalTo(6));

        requests.deleteSavingGoal(accountId, savingGoalId)
              .statusCode(204);

        requests.fetchSavingGoals(accountId)
              .statusCode(200)
              .body("$", hasSize(0));
    }

    @Test
    @DisplayName("Fetch non existing account")
    void fetchingNonExistingAccount(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-missing@account.local");
        requests.authenticate("bank-account-missing@account.local");

        requests.fetchBankAccount(1000000L)
              .statusCode(404)
              .body("message", equalTo("Bank account is not found"));
    }

    @Test
    @DisplayName("Search bank accounts")
    void searchingBankAccounts(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-search@account.local")
              .withBankAccount("Savings account", "EUR", "savings")
              .withBankAccount("Credit card account", "EUR", "credit_card")
              .withBankAccount("Checking account", "EUR", "default")
              .withDebtor("Employer", "EUR")
              .withCreditor("Netflix", "EUR");
        requests.authenticate("bank-account-search@account.local");

        requests.searchBankAccounts(0, 3, List.of(), "account")
              .statusCode(200)
              .body("info.records", equalTo(3))
              .body("info.pages", equalTo(1))
              .body("info.pageSize", equalTo(3))
              .body("content", hasSize(3))
              .body("content[0].name", equalTo("Checking account"))
              .body("content[1].name", equalTo("Credit card account"))
              .body("content[2].name", equalTo("Savings account"));

        requests.searchBankAccounts(0, 3, List.of("creditor"), null)
              .statusCode(200)
              .body("info.records", equalTo(1))
              .body("info.pages", equalTo(1))
              .body("info.pageSize", equalTo(3))
              .body("content", hasSize(1))
              .body("content[0].name", equalTo("Netflix"));
    }

    @Test
    @DisplayName("Reconcile account")
    void reconcileAccount(PledgerContext pledgerContext, PledgerRequests requests) {
        pledgerContext.withUser("bank-account-reconcile@account.local")
            .withBankAccount("Checking account", "EUR", "default");
        requests.authenticate("bank-account-reconcile@account.local");

        var accountId = requests.searchBankAccounts(0, 1, List.of(), "Checking account")
            .statusCode(200)
            .body("content", hasSize(1))
            .extract().jsonPath().getLong("content[0].id");

        requests.reconcileAccount(accountId, 2023, 50.00, 200.0)
            .statusCode(200)
            .body("computed.start", equalTo(0F));

        requests.reconcileAccount(accountId, 2022, 0, 50)
            .statusCode(204);

        requests.reconcileAccount(accountId, 2024, 200, 150)
            .statusCode(204);

        pledgerContext.cleanStorage();
    }
}
