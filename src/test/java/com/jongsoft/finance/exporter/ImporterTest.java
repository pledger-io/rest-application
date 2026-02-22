package com.jongsoft.finance.exporter;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DisplayName("Regression - Profile import")
class ImporterTest extends RestTestSetup {

    @Test
    @DisplayName("Import a sample profile")
    void importSampleProfile(PledgerContext context, PledgerRequests requests) throws IOException {
        context.withUser("sample-profile-import@account.local")
            .withStorage();
        requests.authenticate("sample-profile-import@account.local");

        requests.importProfile("/exporter/importer/profile-sample.json")
            .statusCode(204);

        requests.searchBankAccounts(0, 10, List.of("default"), "Checking account")
            .statusCode(200)
            .body("content", hasSize(1))
            .body("content.name", hasItems("Checking account"))
            .body("content.account.bic", hasItems("COBADEFFXXX"))
            .body("content.account.iban", hasItems("DE89370400440532013000"));

        requests.searchBankAccounts(0, 10, List.of("debtor"), "Big Spender Enc.")
            .statusCode(200)
            .body("content", hasSize(1))
            .body("content.name", hasItems("Big Spender Enc."))
            .body("content.account.bic", hasItems("8DHE2DH"))
            .body("content.account.iban", hasItems("DE893704001230532013000"));

        requests.searchContracts("netflix", "ACTIVE")
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("Netflix updated subscription"))
            .body("[0].company.name", equalTo("Netflix"))
            .body("[0].start", equalTo("2026-01-01"));

        requests.searchTags("s")
            .statusCode(200)
            .body("$", hasSize(4))
            .body("$", hasItems("drinks", "groceries", "shopping", "streaming"));
    }

    @Test
    @DisplayName("Import CSV transactions")
    void importCSVTransactions(PledgerContext context, PledgerRequests requests) {
        context.withUser("csv-transaction-import@account.local")
            .withBankAccount("Checking account", "EUR", "default");
        requests.authenticate("csv-transaction-import@account.local");

        long accountId = requests.searchBankAccounts(0, 1, List.of("default"), "Checking account")
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");

        String configCode = requests.createAttachment("/exporter/configuration/valid-config.json")
            .statusCode(201)
            .extract().body().jsonPath().getString("fileCode");

        requests.createBatchConfig("test-config", "CSVImportProvider", configCode)
            .statusCode(201)
            .body("name", equalTo("test-config"))
            .body("type", equalTo("CSVImportProvider"))
            .body("fileCode", equalTo(configCode));

        String batchFileCode = requests.createAttachment("/exporter/csv-files/single-deposit.csv")
            .statusCode(201)
            .body("fileCode", notNullValue())
            .extract().body().jsonPath().getString("fileCode");

        String batchSlug = requests.createBatchJob("test-config", batchFileCode)
            .statusCode(201)
            .body("slug", notNullValue())
            .body("config.name", equalTo("test-config"))
            .body("finished", nullValue())
            .extract().jsonPath().getString("slug");

        await()
            .atMost(15, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> requests.fetchBatchTasks(batchSlug).extract().statusCode() == 200);

        Map<String, Object> configuration = requests.fetchBatchTasks(batchSlug)
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("configuration"))
            .extract().jsonPath().getMap("[0].variables");

        configuration.put("accountId", accountId);

        requests.completeBatchTask(batchSlug, "configuration", configuration)
            .statusCode(204);

        await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> requests.fetchBatchTasks(batchSlug).extract().statusCode() == 200);

        Map<String, Object> accountMappings = requests.fetchBatchTasks(batchSlug)
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].id", equalTo("Janssen PA"))
            .body("[0].name", equalTo("account-mapping"))
            .body("[0].variables", notNullValue())
            .extract().jsonPath().getMap("[0].variables");

        context.withBankAccount("Janssen PA", "EUR", "debtor");
        long creditorId = requests.searchBankAccounts(0, 1, List.of("debtor"), "Janssen PA")
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");

        accountMappings.put("accountId", creditorId);

        requests.completeBatchTask(batchSlug, "Janssen PA", accountMappings)
            .statusCode(204);

        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(
                        () ->
                                requests.fetchBatchJob(batchSlug)
                                                .extract()
                                                .jsonPath()
                                                .get("finished") != null);
    }

    @Test
    @DisplayName("Import with auto-create accounts")
    void importWithAutoCreate(PledgerContext context, PledgerRequests requests) {
        context.withUser("csv-transaction-import-create@account.local")
            .withBankAccount("Checking account", "EUR", "default");
        requests.authenticate("csv-transaction-import-create@account.local");

        long accountId = requests.searchBankAccounts(0, 1, List.of("default"), "Checking account")
            .statusCode(200)
            .extract().jsonPath().getLong("content[0].id");

        String configCode = requests.createAttachment("/exporter/configuration/valid-config.json")
            .statusCode(201)
            .extract().body().jsonPath().getString("fileCode");

        requests.createBatchConfig("test-config", "CSVImportProvider", configCode)
            .statusCode(201)
            .body("name", equalTo("test-config"))
            .body("type", equalTo("CSVImportProvider"))
            .body("fileCode", equalTo(configCode));

        String batchFileCode = requests.createAttachment("/exporter/csv-files/single-deposit.csv")
            .statusCode(201)
            .body("fileCode", notNullValue())
            .extract().body().jsonPath().getString("fileCode");

        String batchSlug = requests.createBatchJob("test-config", batchFileCode)
            .statusCode(201)
            .body("slug", notNullValue())
            .body("config.name", equalTo("test-config"))
            .body("finished", nullValue())
            .extract().jsonPath().getString("slug");

        await()
            .atMost(15, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(() -> requests.fetchBatchTasks(batchSlug).extract().statusCode() == 200);

        Map<String, Object> configuration = requests.fetchBatchTasks(batchSlug)
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].name", equalTo("configuration"))
            .extract().jsonPath().getMap("[0].variables");

        configuration.put("accountId", accountId);
        configuration.put("generateAccounts", true);
        configuration.put("applyRules", true);

        requests.completeBatchTask(batchSlug, "configuration", configuration)
            .statusCode(204);

        await().atMost(30, TimeUnit.SECONDS)
            .pollInterval(1, TimeUnit.SECONDS)
            .until(
                () ->
                    requests.fetchBatchJob(batchSlug)
                        .extract()
                        .jsonPath()
                        .get("finished") != null);
    }
}
