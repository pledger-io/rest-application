package com.jongsoft.finance.exporter;

import com.jongsoft.finance.RestTestSetup;
import com.jongsoft.finance.extension.PledgerContext;
import com.jongsoft.finance.extension.PledgerRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

@DisplayName("Regression - Profile import")
class ImporterTest extends RestTestSetup {

    @Test
    @DisplayName("Import a sample profile")
    void importSampleProfile(PledgerContext context, PledgerRequests requests) throws IOException {
        context.withUser("sample-profile-import@account.local")
            .withStorage();

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
}
