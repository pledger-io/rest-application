package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ImportProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.core.reflect.ReflectionUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.mutable.MutableLong;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.*;

@DisplayName("CSV Import feature")
public class ImportJobIT extends ProcessTestSetup {

    public static final String IMPORT_JOB_SLUG = "defd23da-76b5-472f-b38d-0bae293dd7dd";
    public static final String CSV_FILE_CODE = "15d45837-bb76-4d36-86da-3fee43de02b7";
    public static final String JSON_FILE_CODE = "8430595d-6e3e-4e92-b243-138227b36475";
    public static final long TARGET_ACCOUNT_ID = 1L;

    @Inject
    private CurrentUserProvider authenticationFacade;
    @Inject
    private AccountProvider accountProvider;
    @Inject
    private ImportProvider importProvider;
    @Inject
    private FilterFactory filterFactory;
    @Inject
    private StorageService storageService;
    @Inject
    private ProcessEngine processEngine;
    @Inject
    private TransactionCreationHandler transactionCreationHandler;
    @Inject
    private TransactionProvider transactionProvider;

    private List<String> tokenCleanup;
    private AccountProvider.FilterCommand filterCommand;

    @BeforeEach
    void setup() {
        filterCommand = Mockito.mock(AccountProvider.FilterCommand.class, Mockito.RETURNS_DEEP_STUBS);
        UserAccount userAccount = Mockito.spy(UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(Collections.List(new Role("admin")))
                .build());

        Mockito.reset(accountProvider, filterFactory, storageService, transactionCreationHandler, transactionProvider, authenticationFacade);

        Mockito.when(authenticationFacade.currentUser()).thenReturn(userAccount);
        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(Control.Option(original));
            tokenCleanup.add(token);
            return token;
        });

        Mockito.when(filterFactory.account()).thenReturn(filterCommand);
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of());

        // setup the transaction creation handler
        MutableLong id = new MutableLong(1);
        Mockito.doAnswer((Answer<Long>) invocation -> {
            CreateTransactionCommand event = invocation.getArgument(0);
            long transactionId = id.getAndAdd(1);

            var field = ReflectionUtils.getRequiredField(Transaction.class, "id");
            field.setAccessible(true);
            field.set(event.transaction(), transactionId);
            Mockito.when(transactionProvider.lookup(transactionId)).thenReturn(Control.Option(event.transaction()));
            return transactionId;
        }).when(transactionCreationHandler).handleCreatedEvent(Mockito.any());

        // setup the account creation
        MutableLong mutableLong = new MutableLong(100);
        Mockito.doAnswer(args -> {
            var account = Account.builder()
                    .id(mutableLong.getAndIncrement())
                    .name(args.getArgument(0, String.class))
                    .currency(args.getArgument(1, String.class))
                    .type(args.getArgument(2, String.class))
                    .build();
            Mockito.when(accountProvider.lookup(args.getArgument(0, String.class))).thenReturn(Control.Option(account));
            Mockito.when(accountProvider.lookup(account.getId())).thenReturn(Control.Option(account));
            return null;
        }).when(userAccount).createAccount(Mockito.anyString(), Mockito.any(), Mockito.anyString());

        Mockito.when(transactionProvider.similar(Mockito.any(), Mockito.any(), Mockito.anyDouble(), Mockito.any()))
                .thenReturn(Collections.List());

        tokenCleanup = new ArrayList<>();
    }

    @Test
    @DisplayName("No target account found, stuck on configuration step")
    void runImportAccountNotFound() {
        // Given:
        Mockito.when(accountProvider.lookup(TARGET_ACCOUNT_ID))
                .thenReturn(Control.Option());
        Mockito.when(importProvider.lookup(IMPORT_JOB_SLUG))
                .thenReturn(Control.Option(createBatchImport()));
        Mockito.when(storageService.read(JSON_FILE_CODE))
                .thenReturn(Control.Option(readResource("/import-test/import-config-test.json")));

        // When:
        var process = processEngine.getRuntimeService().startProcessInstanceByKey("import_job",
                Variables.createVariables()
                        .putValue("importJobSlug", IMPORT_JOB_SLUG)
        );

        completeImportConfig(process, true);

        // Then:
        var configureJsonTask = processEngine.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("task_configure")
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();
        Assertions.assertThat(configureJsonTask).isNotNull();

        // Then:
        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verifyNoMoreInteractions(accountProvider);
    }

    @Test
    @DisplayName("Run with manual account creation for Pieterse")
    void runWithManualAccountCreate() {
        // Given:
        Mockito.when(accountProvider.lookup(TARGET_ACCOUNT_ID))
                .thenReturn(Control.Option(createTargetAccount()));
        Mockito.when(importProvider.lookup(IMPORT_JOB_SLUG))
                .thenReturn(Control.Option(createBatchImport()));
        Mockito.when(storageService.read(JSON_FILE_CODE))
                .thenReturn(Control.Option(readResource("/import-test/import-config-test.json")));
        Mockito.when(storageService.read(CSV_FILE_CODE))
                .thenReturn(Control.Option(readResource("/import-test/import-test.csv")));

        // Default account lookup shall yield no results
        Mockito.when(accountProvider.lookup(Mockito.anyString()))
                .thenReturn(Control.Option());
        Mockito.when(accountProvider.synonymOf(Mockito.anyString()))
                .thenReturn(Control.Option());
        setupPostAccount();
        setupJanssenAccount();

        // When:
        var process = processEngine.getRuntimeService().startProcessInstanceByKey("import_job",
                Variables.createVariables()
                        .putValue("importJobSlug", IMPORT_JOB_SLUG)
        );

        completeImportConfig(process, false);
        completeAccountMapping(process);
        completeAccountCreationPieterse(process);

        // Then:
        Mockito.verify(accountProvider).lookup("Janssen PA");
        Mockito.verify(accountProvider).lookup("P. Post");

        verifyStorageCleaned();

        var createdTransactions = Mockito.mockingDetails(transactionCreationHandler).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("handleCreatedEvent"))
                .map(invocation -> invocation.getArgument(0, CreateTransactionCommand.class))
                .map(CreateTransactionCommand::transaction)
                .toList();

        Assertions.assertThat(createdTransactions)
                .hasSize(4)
                .anySatisfy(this::verifyPostTransaction)
                .anySatisfy(this::verifyJanssenTransaction)
                .anySatisfy(this::verifyPieterseTransaction);
    }

    @Test
    @DisplayName("Run with automated account creation")
    void runWithAutomatedAccountCreation() {
        // Given:
        Mockito.when(accountProvider.lookup(TARGET_ACCOUNT_ID))
                .thenReturn(Control.Option(createTargetAccount()));
        Mockito.when(importProvider.lookup(IMPORT_JOB_SLUG))
                .thenReturn(Control.Option(createBatchImport()));
        Mockito.when(storageService.read(JSON_FILE_CODE))
                .thenReturn(Control.Option(readResource("/import-test/import-config-test.json")));
        Mockito.when(storageService.read(CSV_FILE_CODE))
                .thenReturn(Control.Option(readResource("/import-test/import-test.csv")));

        // Default account lookup shall yield no results
        Mockito.when(accountProvider.lookup(Mockito.anyString()))
                .thenReturn(Control.Option());
        Mockito.when(accountProvider.synonymOf(Mockito.anyString()))
                .thenReturn(Control.Option());
        setupPostAccount();
        setupJanssenAccount();

        // When:
        var process = processEngine.getRuntimeService().startProcessInstanceByKey("import_job",
                Variables.createVariables()
                        .putValue("importJobSlug", IMPORT_JOB_SLUG)
        );

        completeImportConfig(process, true);
        completeAccountMapping(process);

        // Then:
        Mockito.verify(accountProvider).lookup("Janssen PA");
        Mockito.verify(accountProvider).lookup("P. Post");

        verifyStorageCleaned();

        var createdTransactions = Mockito.mockingDetails(transactionCreationHandler).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("handleCreatedEvent"))
                .map(invocation -> invocation.getArgument(0, CreateTransactionCommand.class))
                .map(CreateTransactionCommand::transaction)
                .toList();

        Assertions.assertThat(createdTransactions)
                .hasSize(4)
                .anySatisfy(this::verifyPostTransaction)
                .anySatisfy(this::verifyJanssenTransaction)
                .anySatisfy(this::verifyPieterseTransaction);
    }

    private void verifyStorageCleaned() {
        var captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(storageService, Mockito.times(tokenCleanup.size())).remove(captor.capture());

        Assertions.assertThat(captor.getAllValues())
                .containsExactlyInAnyOrderElementsOf(tokenCleanup);
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

    private void completeImportConfig(ProcessInstance process, boolean allowCreate) {
        var configureJsonTask = processEngine.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("task_configure")
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();
        Assertions.assertThat(configureJsonTask).isNotNull();

        var importConfig = (ImportConfigJson) processEngine.getTaskService()
                .getVariable(configureJsonTask.getId(), "initialConfig");

        importConfig.setAccountId(TARGET_ACCOUNT_ID);
        importConfig.setGenerateAccounts(allowCreate);

        processEngine.getTaskService()
                .complete(configureJsonTask.getId(), Variables.createVariables()
                        .putValue("updatedConfig", importConfig));
    }

    private void completeAccountMapping(ProcessInstance process) {
        var configureJsonTask = processEngine.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("confirm_mappings")
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();
        Assertions.assertThat(configureJsonTask).isNotNull();

        @SuppressWarnings("unchecked")
        var accountMappings = (Set<ExtractionMapping>) processEngine.getTaskService()
                .getVariable(configureJsonTask.getId(), "account_mappings");

        Assertions.assertThat(accountMappings)
                .hasSize(3)
                .anySatisfy(mapping -> {
                    Assertions.assertThat(mapping.getName()).isEqualTo("Janssen PA");
                    Assertions.assertThat(mapping.getAccountId()).isEqualTo(3L);
                })
                .anySatisfy(mapping -> {
                    Assertions.assertThat(mapping.getName()).isEqualTo("P. Post");
                    Assertions.assertThat(mapping.getAccountId()).isEqualTo(2L);
                })
                .anySatisfy(mapping -> {
                    Assertions.assertThat(mapping.getName()).isEqualTo("MW GA Pieterse");
                    Assertions.assertThat(mapping.getAccountId()).isNull();
                });

        processEngine.getTaskService()
                .complete(configureJsonTask.getId(), Variables.createVariables()
                        .putValue("account_mappings", accountMappings));
    }

    private void completeAccountCreationPieterse(ProcessInstance process) {
        var configureJsonTask = processEngine.getTaskService()
                .createTaskQuery()
                .taskDefinitionKey("user_create_account")
                .processInstanceId(process.getProcessInstanceId())
                .singleResult();
        Assertions.assertThat(configureJsonTask).isNotNull();

        Assertions.assertThat(processEngine.getTaskService().getVariable(configureJsonTask.getId(), "accountName"))
                .isEqualTo("MW GA Pieterse");
        Assertions.assertThat(processEngine.getTaskService().getVariable(configureJsonTask.getId(), "basedOnAccount"))
                .isEqualTo(1L);

        setupPieterseAccount();

        processEngine.getTaskService()
                .complete(configureJsonTask.getId(), Variables.createVariables()
                        .putValue("accountId", 4L));
    }

    private Account createTargetAccount() {
        return Account.builder()
                .id(1L)
                .iban("DE89370400440532013000")
                .name("Test Account")
                .build();
    }

    private void setupPostAccount() {
        var account = Account.builder()
                .id(2L)
                .iban("NL69INGB0123456789")
                .name("P. Post")
                .build();

        Mockito.when(accountProvider.lookup(account.getName()))
                .thenReturn(Control.Option(account));
        Mockito.when(accountProvider.lookup(account.getId()))
                .thenReturn(Control.Option(account));
    }

    private void setupJanssenAccount() {
        var account = Account.builder()
                .id(3L)
                .iban("NL69INGB0123456789")
                .name("Janssen PA")
                .build();

        Mockito.when(accountProvider.lookup(account.getName()))
                .thenReturn(Control.Option(account));
        Mockito.when(accountProvider.lookup(account.getId()))
                .thenReturn(Control.Option(account));
    }

    private void setupPieterseAccount() {
        var account = Account.builder()
                .id(4L)
                .iban("NL71INGB0009876543")
                .name("MW GA Pieterse")
                .build();

        Mockito.when(accountProvider.lookup(account.getName()))
                .thenReturn(Control.Option(account));
        Mockito.when(accountProvider.lookup(account.getId()))
                .thenReturn(Control.Option(account));
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

    private byte[] readResource(String name) {
        return Control.Option(getClass().getResourceAsStream(name))
                .map(stream -> Control.Try(stream::readAllBytes).get())
                .getOrThrow(() -> new IllegalStateException("Cannot read resource " + name));
    }
}
