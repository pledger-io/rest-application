package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping;
import com.jongsoft.finance.bpmn.process.ProcessExtension;
import com.jongsoft.finance.bpmn.process.RuntimeContext;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@MicronautTest
@ProcessExtension
@DisplayName("CSV Import feature")
public class ImportJobIT {

    public static final String IMPORT_JOB_SLUG = "defd23da-76b5-472f-b38d-0bae293dd7dd";
    public static final String CSV_FILE_CODE = "15d45837-bb76-4d36-86da-3fee43de02b7";
    public static final String JSON_FILE_CODE = "8430595d-6e3e-4e92-b243-138227b36475";
    public static final long TARGET_ACCOUNT_ID = 1L;

    @Test
    @DisplayName("No target account found, stuck on configuration step")
    void runImportAccountNotFound(RuntimeContext context) {
        context
            .withStorage(JSON_FILE_CODE, "/import-test/import-config-test.json")
            .withStorage(CSV_FILE_CODE, "/import-test/import-test.csv")
            .withImportJob(createBatchImport());

        var process = context.execute("import_job", Map.of(
                "importJobSlug", IMPORT_JOB_SLUG
        ));

        process
            .task("task_configure")
            .<ImportConfigJson>updateVariable("initialConfig", "updatedConfig", config -> {
                config.setAccountId(TARGET_ACCOUNT_ID);
                return config;
            })
            .complete();

        // Task should be active again
        process.task("task_configure");
    }

    @Test
    @DisplayName("Run with account mapping adjustments")
    void runWithAccountMappingAdjustments(RuntimeContext context) {
        context
            .withStorage()
            .withStorage(JSON_FILE_CODE, "/import-test/import-config-test.json")
            .withStorage(CSV_FILE_CODE, "/import-test/import-test.csv")
            .withAccount(createTargetAccount())
            .withAccount(setupPostAccount())
            .withAccount(setupPieterseAccount())
            .withImportJob(createBatchImport())
            .withTransactions();

        var process = context.execute("import_job", Map.of(
                "importJobSlug", IMPORT_JOB_SLUG
        ));

        process.task("task_configure")
                .<ImportConfigJson>updateVariable("initialConfig", "updatedConfig", config -> {
                    config.setAccountId(TARGET_ACCOUNT_ID);
                    return config;
                })
                .complete();

        process.task("confirm_mappings")
                .<Set<ExtractionMapping>>verifyVariable("account_mappings", mappings ->
                        Assertions.assertThat(mappings)
                                .hasSize(3)
                                .anySatisfy(mapping -> {
                                    Assertions.assertThat(mapping.getName()).isEqualTo("P. Post");
                                    Assertions.assertThat(mapping.getAccountId()).isEqualTo(2L);
                                })
                                .anySatisfy(mapping -> {
                                    Assertions.assertThat(mapping.getName()).isEqualTo("MW GA Pieterse");
                                    Assertions.assertThat(mapping.getAccountId()).isEqualTo(4L);
                                }))
                .complete(Map.of("account_mappings", Set.of(
                        new ExtractionMapping("P. Post", 2L),
                        new ExtractionMapping("Janssen PA", 2L),
                        new ExtractionMapping("MW GA Pieterse", 4L)
                )));

        context.verifyTransactions(assertion -> assertion.hasSize(4)
                        .anySatisfy(this::verifyPostTransaction)
                        .noneSatisfy(this::verifyJanssenTransaction)
                        .anySatisfy(this::verifyPieterseTransaction));
    }

    @Test
    @DisplayName("Run with manual account creation for Pieterse")
    void runWithManualAccountCreate(RuntimeContext context) {
        context
            .withStorage()
            .withStorage(JSON_FILE_CODE, "/import-test/import-config-test.json")
            .withStorage(CSV_FILE_CODE, "/import-test/import-test.csv")
            .withAccount(createTargetAccount())
            .withAccount(setupPostAccount())
            .withAccount(setupJanssenAccount())
            .withImportJob(createBatchImport())
            .withTransactions();

        var process = context.execute("import_job", Map.of(
                "importJobSlug", IMPORT_JOB_SLUG
        ));

        process.task("task_configure")
                .<ImportConfigJson>updateVariable("initialConfig", "updatedConfig", config -> {
                    config.setAccountId(TARGET_ACCOUNT_ID);
                    config.setGenerateAccounts(false);
                    return config;
                })
                .complete();

        process.task("confirm_mappings")
                .<Set<ExtractionMapping>>verifyVariable("account_mappings", mappings ->
                        Assertions.assertThat(mappings)
                                .hasSize(3)
                                .anySatisfy(mapping -> {
                                    Assertions.assertThat(mapping.getName()).isEqualTo("MW GA Pieterse");
                                    Assertions.assertThat(mapping.getAccountId()).isNull();
                                })
                )
                .complete();
        context.withAccount(setupPieterseAccount());

        process.task("user_create_account")
                .complete(Map.of("accountId", 4L));

        context.verifyTransactions(assertion -> assertion.hasSize(4)
                .anySatisfy(this::verifyPostTransaction)
                .anySatisfy(this::verifyJanssenTransaction)
                .anySatisfy(this::verifyPieterseTransaction));
    }

    @Test
    @DisplayName("Run with automated account creation")
    void runWithAutomatedAccountCreation(RuntimeContext context) {
        context
            .withStorage()
            .withStorage(JSON_FILE_CODE, "/import-test/import-config-test.json")
            .withStorage(CSV_FILE_CODE, "/import-test/import-test.csv")
            .withAccounts()
            .withAccount(createTargetAccount())
            .withAccount(setupPostAccount())
            .withAccount(setupJanssenAccount())
            .withImportJob(createBatchImport())
            .withTransactions();

        var process = context.execute("import_job", Map.of(
                "importJobSlug", IMPORT_JOB_SLUG
        ));

        process.task("task_configure")
                .<ImportConfigJson>updateVariable("initialConfig", "updatedConfig", config -> {
                    config.setAccountId(TARGET_ACCOUNT_ID);
                    config.setGenerateAccounts(true);
                    return config;
                })
                .complete();

        process.task("confirm_mappings")
                .updateVariable("account_mappings", "account_mappings", mappings -> mappings)
                .complete();

        context
                .verifyTransactions(assertion -> assertion.hasSize(4)
                        .anySatisfy(this::verifyPostTransaction)
                        .anySatisfy(this::verifyJanssenTransaction)
                        .anySatisfy(this::verifyPieterseTransaction))
                .verifyStorageCleaned();

        context.verifyInteraction(AccountProvider.class).lookup("Janssen PA");
        context.verifyInteraction(AccountProvider.class).lookup("P. Post");
    }

    private void verifyPostTransaction(Transaction transaction) {
        Assertions.assertThat(transaction.getDescription()).isEqualTo("Naam: P. Post Omschrijving: Factuur 123 IBAN: NL69INGB0123456789 Kenmerk: 190451787399");
        Assertions.assertThat(transaction.getTransactions())
                .hasSize(2)
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("P. Post");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(-14.19);
                })
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("Test Account");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(14.19);
                });
    }

    private void verifyJanssenTransaction(Transaction transaction) {
        Assertions.assertThat(transaction.getDescription()).isEqualTo("Naam: P. Post Omschrijving: Factuur 123 IBAN: NL69INGB0123456789 Kenmerk: 190451787399");
        Assertions.assertThat(transaction.getTransactions())
                .hasSize(2)
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("Janssen PA");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(-12.19);
                })
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("Test Account");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(12.19);
                });
    }

    private void verifyPieterseTransaction(Transaction transaction) {
        Assertions.assertThat(transaction.getDescription()).isEqualTo("Naam: Mw G A Pieterse Omschrijving: inzake bestelling IBAN: NL71INGB0009876543");
        Assertions.assertThat(transaction.getTransactions())
                .hasSize(2)
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("MW GA Pieterse");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(283.90);
                })
                .anySatisfy(subTransaction -> {
                    Assertions.assertThat(subTransaction.getAccount().getName()).isEqualTo("Test Account");
                    Assertions.assertThat(subTransaction.getAmount()).isEqualTo(-283.90);
                });
    }

    private Account createTargetAccount() {
        return Account.builder()
                .id(1L)
                .iban("DE89370400440532013000")
                .name("Test Account")
                .build();
    }

    private Account setupPostAccount() {
        return Account.builder()
                .id(2L)
                .iban("NL69INGB0123456789")
                .name("P. Post")
                .build();
    }

    private Account setupJanssenAccount() {
        return Account.builder()
                .id(3L)
                .iban("NL69INGB0123456789")
                .name("Janssen PA")
                .build();
    }

    private Account setupPieterseAccount() {
        return Account.builder()
                .id(4L)
                .iban("NL71INGB0009876543")
                .name("MW GA Pieterse")
                .build();
    }


    private BatchImport createBatchImport() {
        return BatchImport.builder()
                .id(1L)
                .slug(IMPORT_JOB_SLUG)
                .created(new Date())
                .fileCode(CSV_FILE_CODE)
                .config(BatchImportConfig.builder()
                        .fileCode(JSON_FILE_CODE)
                        .build())
                .build();
    }
}
