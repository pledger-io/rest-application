package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.delegate.importer.ExtractionMapping;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.importer.ImportProvider;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.ImportConfigJson;
import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.collection.Set;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImportAccountExtractorIT extends ProcessTestSetup {
    private class AccountFilterTest implements AccountProvider.FilterCommand {

        private Sequence<String> calls = API.List();

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
    private CurrentUserProvider userService;
    @Inject
    private UserProvider userProvider;
    @Inject
    private FilterFactory filterFactory;

    @Inject
    private ImportProvider importProvider;
    @Inject
    private AccountProvider accountProvider;
    @Inject
    private StorageService storageService;

    @Inject
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        Mockito.reset(userService, accountProvider, storageService, importProvider, userProvider, filterFactory);

        final UserAccount userAccount = UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(API.List(new Role("admin")))
                .build();
        Mockito.when(userService.currentUser()).thenReturn(userAccount);
        Mockito.when(userProvider.lookup("test-user")).thenReturn(API.Option(userAccount));

        Mockito.when(accountProvider.lookup("MW GA Pieterse"))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(Account.builder()
                        .id(2L)
                        .name("MW GA Pieterse")
                        .type("creditor")
                        .build()));

        Mockito.when(filterFactory.account()).thenAnswer(args -> new AccountFilterTest());
        Mockito.when(accountProvider.synonymOf(Mockito.anyString())).thenReturn(Maybe.empty());
        Mockito.when(accountProvider.lookup(Mockito.anyString())).thenReturn(Maybe.empty());
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.empty());

        Mockito.when(accountProvider.lookup(new AccountFilterTest().iban("NL69INGB0123456789", true)))
                .thenReturn(ResultPage.of(Account.builder()
                        .name("P. Post")
                        .id(3L)
                        .type("debtor")
                        .build()));

        Mockito.when(accountProvider.synonymOf("Janssen PA"))
                .thenReturn(Maybe.just(Account.builder()
                        .id(5L)
                        .name("Jansen PA")
                        .type("creditor")
                        .build()));
    }

    @Test
    @Deployment(resources = {"/bpmn/transaction/transactions.extract.accounts.bpmn"})
    @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_NONE)
    public void run() {
        ImportConfigJson importConfigJson = ImportConfigJson.read(new String(readFile("import-test/import-config-test.json")));

        BatchImport batchImport = BatchImport.builder()
                .id(1L)
                .slug("account-test-import")
                .created(new Date(2019, 1, 1))
                .fileCode("sample-file-run")
                .build();

        Mockito.when(importProvider.lookup("account-test-import")).thenReturn(Maybe.just(batchImport));
        Mockito.when(storageService.read("sample-file-run")).thenReturn(readFile("import-test/import-test.csv"));
        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(original);
            return token;
        });

        final ProcessInstanceWithVariables response = processEngine.getRuntimeService().createProcessInstanceByKey("ImportExtractAccounts")
                .setVariable("slug", batchImport.getSlug())
                .setVariable("importConfig", importConfigJson)
                .setVariable("username", userService.currentUser().getUsername())
                .businessKey("sample-key")
                .executeWithVariablesInReturn();

        waitUntilNoActiveJobs(processEngine, 1000);
        Mockito.verify(importProvider).lookup("account-test-import");
        Mockito.verify(accountProvider).lookup("MW GA Pieterse");
        Mockito.verify(accountProvider).lookup(new AccountFilterTest().iban("NL69INGB0123456789", true));
        Mockito.verify(accountProvider, Mockito.never()).lookup("P. Post");
        Mockito.verify(accountProvider, Mockito.times(2)).synonymOf("Janssen PA");

        final List<HistoricVariableInstance> variables = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(response.getProcessInstanceId())
                .variableName("transactions")
                .list();

        Assertions.assertThat(variables).hasSize(1);

        Set transactions = API.Set((Iterable) variables.get(0).getValue());
        Assertions.assertThat(transactions.get(0)).isInstanceOf(ExtractionMapping.class);
        Assertions.assertThat(transactions.get(1)).isInstanceOf(ExtractionMapping.class);
        Assertions.assertThat(transactions.get(2)).isInstanceOf(ExtractionMapping.class);
    }

    private byte[] readFile(String file) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(file);
        return new BufferedReader(new InputStreamReader(resourceAsStream))
                .lines()
                .collect(Collectors.joining("\n"))
                .getBytes();
    }
}
