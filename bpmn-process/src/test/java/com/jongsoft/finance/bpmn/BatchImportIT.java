package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionCreationHandler;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.core.reflect.ReflectionUtils;
import io.reactivex.Maybe;
import org.apache.commons.lang3.mutable.MutableLong;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BatchImportIT extends ProcessTestSetup {

    private class AccountFilterTest implements AccountProvider.FilterCommand {

        private Sequence<String> calls = API.List();

        @Override
        public String toString() {
            return "AccountFilterTest{" +
                    "calls=" + calls +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AccountFilterTest other) {
                return Objects.equals(calls, other.calls);
            }

            return false;
        }

        @Override
        public AccountProvider.FilterCommand name(String value, boolean fullMatch) {
            calls = calls.append("name-" + value + fullMatch);
            return this;
        }

        @Override
        public AccountProvider.FilterCommand iban(String value, boolean fullMatch) {
            calls = calls.append("iban-" + value + fullMatch);
            return this;
        }

        @Override
        public AccountProvider.FilterCommand number(String value, boolean fullMatch) {
            calls = calls.append("number-" + value + fullMatch);
            return this;
        }

        @Override
        public AccountProvider.FilterCommand types(Sequence<String> types) {
            calls = calls.append("types-" + types.toString());
            return this;
        }

        @Override
        public AccountProvider.FilterCommand page(int value) {
            calls = calls.append("page-" + value);
            return this;
        }

        @Override
        public AccountProvider.FilterCommand pageSize(int value) {
            calls = calls.append("pageSize-" + value);
            return this;
        }
    }

    @Inject
    private ProcessEngine processEngine;
    @Inject
    private CurrentUserProvider authenticationFacade;

    @Inject
    private ImportProvider importProvider;
    @Inject
    private AccountProvider accountProvider;
    @Inject
    private TransactionProvider transactionProvider;
    @Inject
    private TransactionRuleProvider transactionRuleProvider;
    @Inject
    private UserProvider userProvider;
    @Inject
    private StorageService storageService;
    @Inject
    private TransactionCreationHandler transactionCreationHandler;
    @Inject
    private FilterFactory accountFilterFactory;

    @BeforeEach
    void setup() {
        Mockito.reset(
                authenticationFacade,
                accountProvider,
                storageService,
                importProvider,
                transactionProvider,
                transactionCreationHandler,
                transactionRuleProvider,
                accountFilterFactory);
        UserAccount userAccount = Mockito.spy(UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(API.List(new Role("admin")))
                .build());
        Mockito.when(authenticationFacade.currentUser()).thenReturn(userAccount);
        Mockito.when(userProvider.lookup("test-user")).thenReturn(API.Option(userAccount));

        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(original);
            return token;
        });

        MutableLong mutableLong = new MutableLong(100);
        Mockito.doAnswer(args -> {
            var account = Account.builder()
                    .id(mutableLong.getAndIncrement())
                    .name(args.getArgument(0, String.class))
                    .currency(args.getArgument(1, String.class))
                    .type(args.getArgument(2, String.class))
                    .build();
            Mockito.when(accountProvider.lookup(args.getArgument(0, String.class))).thenReturn(API.Option(account));
            Mockito.when(accountProvider.lookup(account.getId())).thenReturn(API.Option(account));
            return null;
        }).when(userAccount).createAccount(Mockito.anyString(), Mockito.any(), Mockito.anyString());

        Mockito.when(transactionRuleProvider.lookup()).thenReturn(API.List());
        Mockito.when(accountFilterFactory.account()).thenAnswer(args -> new AccountFilterTest());
        Mockito.when(transactionProvider.similar(Mockito.any(), Mockito.any(), Mockito.anyDouble(), Mockito.any()))
                .thenReturn(API.List());
    }

    @Test
    @Deployment(resources = {"/bpmn/transaction/transactions.import.bpmn", "/bpmn/transaction/transaction.rules.apply.bpmn"})
    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_NONE)
    void run() throws InterruptedException {
        ImportConfigJson importConfigJson = ImportConfigJson.read(new String(readFile("import-test/import-config-test.json")));

        BatchImport batchImport = BatchImport.builder()
                .id(1L)
                .slug("account-test-import")
                .created(new Date(2019, 1, 1))
                .fileCode("sample-file-run")
                .build();

        importConfigJson.setAccountId(1L);

        Mockito.when(storageService.read("sample-file-run")).thenReturn(readFile("import-test/import-test.csv"));
        Mockito.when(importProvider.lookup("account-test-import")).thenReturn(Maybe.just(batchImport));

        Mockito.when(accountProvider.lookup(1L)).thenReturn(API.Option(Account.builder()
                .id(1L)
                .name("Checking account")
                .type("checking")
                .build()));

        final Account pieterseAccount = Account.builder().id(2L).type("creditor").build();
        final Account accountPost = Account.builder().id(3L).name("P. Post").type("debtor").build();
        final Account accountKabel = Account.builder().id(4L).name("KABEL TV").iban("NL31INGB0001122334").type("creditor").build();

        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class))).thenReturn(ResultPage.empty());
        Mockito.when(accountProvider.lookup(Mockito.anyString())).thenReturn(API.Option());
        Mockito.when(accountProvider.lookup(2L)).thenReturn(API.Option(pieterseAccount));
        Mockito.when(accountProvider.lookup(3L)).thenReturn(API.Option(accountPost));

        Mockito.when(accountProvider.lookup("MW GA Pieterse"))
                .thenReturn(API.Option())
                .thenReturn(API.Option())
                .thenReturn(API.Option(pieterseAccount));

        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(API.Option(Account.builder()
                        .id(123L)
                        .name("Janssen PA")
                        .type("creditor")
                        .build()));

        Mockito.when(accountProvider.lookup(new AccountFilterTest().iban("NL69INGB0123456789", true)))
                .thenReturn(ResultPage.of(accountPost));
        Mockito.when(accountProvider.lookup("KABEL TV"))
                .thenReturn(API.Option(accountKabel));
        Mockito.when(accountProvider.lookup(new AccountFilterTest().iban("NL31INGB0001122334", true)))
                .thenReturn(ResultPage.empty())
                .thenReturn(ResultPage.of(accountKabel));

        Mockito.when(storageService.read("account-mapping-token")).thenReturn("{\"Janssen PA\": 123}".getBytes());

        MutableLong id = new MutableLong(1);
        Mockito.when(transactionCreationHandler.handleCreatedEvent(Mockito.any())).thenAnswer((Answer<Long>) invocation -> {
            TransactionCreatedEvent event = invocation.getArgument(0);
            long transactionId = id.getAndAdd(1);

            var field = ReflectionUtils.getRequiredField(Transaction.class, "id");
            field.setAccessible(true);
            field.set(event.getTransaction(), transactionId);
            Mockito.when(transactionProvider.lookup(transactionId)).thenReturn(API.Option(event.getTransaction()));
            return transactionId;
        });

        processEngine.getRuntimeService().createProcessInstanceByKey("BatchTransactionImport")
                .setVariable("slug", batchImport.getSlug())
                .setVariable("importConfig", importConfigJson.write())
                .setVariable("accountMapping", "account-mapping-token")
                .setVariable("username", authenticationFacade.currentUser().getUsername())
                .businessKey("sample-key")
                .execute();

        waitUntilNoActiveJobs(processEngine, 2500);

        Mockito.verify(importProvider, Mockito.times(2)).lookup("account-test-import");
        Mockito.verify(accountProvider, Mockito.times(6)).lookup(123L);
        Mockito.verify(accountProvider, Mockito.times(4)).lookup("MW GA Pieterse");
        Mockito.verify(accountProvider, Mockito.times(1)).lookup(new AccountFilterTest().iban("NL69INGB0123456789", true));
        Mockito.verify(accountProvider, Mockito.never()).lookup("P. Post");
//        Mockito.verify(accountProvider, Mockito.times(2)).lookup(new AccountFilterTest() {
//            @Override
//            public DataProvider.TextFilter iban() {
//                return new TextFilterTest("NL31INGB0001122334");
//            }
//        });

        assertThat(batchImport.getFinished()).isNotNull();
    }

    private byte[] readFile(String file) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(file);
        return new BufferedReader(new InputStreamReader(resourceAsStream))
                .lines()
                .collect(Collectors.joining("\n"))
                .getBytes();
    }
}
