package com.jongsoft.finance.bpmn.process;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.reflect.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableLong;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.camunda.bpm.engine.ProcessEngine;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class RuntimeContext {

    private final ApplicationContext applicationContext;

    private ProcessEngine processEngine;

    private UserAccount userAccount;

    private final List<String> storageTokens;
    private final MutableLong idGenerator;

    public RuntimeContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.storageTokens = new ArrayList<>();
        this.userAccount = Mockito.spy(UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(Collections.List(new Role("admin")))
                .build());
        this.processEngine = applicationContext.getBean(ProcessEngine.class);
        idGenerator = new MutableLong(100);

        resetMocks();
        setupDefaultMocks();
    }

    public RuntimeContext withStorage() {
        var storageService = applicationContext.getBean(StorageService.class);
        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(Control.Option(original));
            storageTokens.add(token);
            return token;
        });
        return this;
    }

    public RuntimeContext withStorage(String token, String resource) {
        log.debug("Stored file with token: {}", token);
        var bytes = Control.Option(getClass().getResourceAsStream(resource))
                .map(stream -> Control.Try(stream::readAllBytes).get())
                .getOrThrow(() -> new IllegalStateException("Cannot read resource " + resource));

        Mockito.when(applicationContext.getBean(StorageService.class).read(token))
                .thenReturn(Control.Option(bytes));

        return this;
    }

    public RuntimeContext withTransactions() {
        var transactionProvider = applicationContext.getBean(TransactionProvider.class);
        var transactionCreationHandler = applicationContext.getBean(TransactionCreationHandler.class);

        Mockito.doAnswer((Answer<Long>) invocation -> {
            CreateTransactionCommand event = invocation.getArgument(0);
            long transactionId = idGenerator.getAndAdd(1);

            var field = ReflectionUtils.getRequiredField(Transaction.class, "id");
            field.setAccessible(true);
            field.set(event.transaction(), transactionId);
            Mockito.when(transactionProvider.lookup(transactionId)).thenReturn(Control.Option(event.transaction()));
            return transactionId;
        }).when(transactionCreationHandler).handleCreatedEvent(Mockito.any());

        return this;
    }

    public RuntimeContext withTags() {
        var tagProvider = applicationContext.getBean(TagProvider.class);

        Mockito.when(userAccount.createTag(Mockito.anyString()))
                .thenAnswer((Answer<Tag>) invocation -> {
                    String name = invocation.getArgument(0);
                    Tag tag = new Tag(name);
                    Mockito.when(tagProvider.lookup(name)).thenReturn(Control.Option(tag));
                    return tag;
                });
        return this;
    }

    public RuntimeContext withAccounts() {
        var accountProvider = applicationContext.getBean(AccountProvider.class);

        Mockito.when(userAccount.createAccount(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenAnswer((Answer<Account>) invocation -> {
                    String name = invocation.getArgument(0);
                    String currency = invocation.getArgument(1);
                    String type = invocation.getArgument(2);
                    long accountId = idGenerator.getAndAdd(1);
                    Account account = Account.builder()
                            .id(accountId)
                            .name(name)
                            .currency(currency)
                            .type(type)
                            .build();
                    Mockito.when(accountProvider.lookup(accountId)).thenReturn(Control.Option(account));
                    Mockito.when(accountProvider.lookup(name)).thenReturn(Control.Option(account));
                    return account;
                });
        return this;
    }

    public RuntimeContext withAccount(Account account) {
        Mockito.when(applicationContext.getBean(AccountProvider.class).lookup(account.getName()))
                .thenReturn(Control.Option(account));
        Mockito.when(applicationContext.getBean(AccountProvider.class).lookup(account.getId()))
                .thenReturn(Control.Option(account));
        return this;
    }

    public RuntimeContext withCategory(String name) {
        var category = Category.builder()
                .id(idGenerator.getAndAdd(1))
                .label(name)
                .build();

        Mockito.when(applicationContext.getBean(CategoryProvider.class).lookup(category.getLabel()))
                .thenReturn(Control.Option(category));
        Mockito.when(applicationContext.getBean(CategoryProvider.class).lookup(category.getId()))
                .thenReturn(Control.Option(category));

        return this;
    }

    public RuntimeContext withImportJob(BatchImport batchImport) {
        Mockito.when(applicationContext.getBean(ImportProvider.class).lookup(batchImport.getSlug()))
                .thenReturn(Control.Option(batchImport));
        return this;
    }

    public ProcessExecution execute(String processKey, Map<String, Object> variables) {
        return new ProcessExecution(processEngine, processKey, variables);
    }

    public RuntimeContext verifyAccountCreated(String name, String currency, String type) {
            Mockito.verify(userAccount, Mockito.atMostOnce())
                    .createAccount(name, currency, type);
        return this;
    }

    public RuntimeContext verifyCategoryCreated(String name) {
        Mockito.verify(userAccount, Mockito.atMostOnce())
                .createCategory(name);
        return this;
    }

    public RuntimeContext verifyTransactions(Consumer<ListAssert<Transaction>> validations) {
        var createdTransactions = Mockito.mockingDetails(applicationContext.getBean(TransactionCreationHandler.class)).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("handleCreatedEvent"))
                .map(invocation -> invocation.getArgument(0, CreateTransactionCommand.class))
                .map(CreateTransactionCommand::transaction)
                .toList();

        validations.accept(Assertions.assertThat(createdTransactions));

        return this;
    }

    public RuntimeContext verifyStorageCleaned() {
        storageTokens.forEach(token ->
                Mockito.verify(applicationContext.getBean(StorageService.class)).remove(token));
        return this;
    }

    public RuntimeContext verifyRuleCreated(String name, Consumer<ObjectAssert<TransactionRule>> validations) {
        var matchedRule = Mockito.mockingDetails(applicationContext.getBean(TransactionRuleProvider.class))
                .getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .map(invocation -> invocation.getArgument(0, TransactionRule.class))
                .filter(rule -> rule.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Rule not found: " +name));

        validations.accept(
                Assertions.assertThat(matchedRule)
                        .as("Rule created for account: " + name));
        return this;
    }

    public RuntimeContext verifyTagCreated(String name) {
        Mockito.verify(userAccount).createTag(name);
        return this;
    }

    public <T> T verifyInteraction(Class<T> type) {
        return Mockito.verify(applicationContext.getBean(type));
    }

    private void resetMocks() {
        Mockito.reset(applicationContext.getBean(AuthenticationFacade.class));
        Mockito.reset(applicationContext.getBean(CurrentUserProvider.class));
        Mockito.reset(applicationContext.getBean(AccountProvider.class));
        Mockito.reset(applicationContext.getBean(TransactionProvider.class));
        Mockito.reset(applicationContext.getBean(ImportProvider.class));
        Mockito.reset(applicationContext.getBean(TransactionCreationHandler.class));
        Mockito.reset(applicationContext.getBean(TransactionRuleProvider.class));
        Mockito.reset(applicationContext.getBean(CategoryProvider.class));
        Mockito.reset(userAccount);
    }
    private void setupDefaultMocks() {
        Mockito.when(applicationContext.getBean(AuthenticationFacade.class).authenticated())
                .thenReturn("test-user");
        Mockito.when(applicationContext.getBean(CurrentUserProvider.class).currentUser())
                .thenReturn(userAccount);

        // Prepare the mocks for the filter factory
        var filterFactory = applicationContext.getBean(FilterFactory.class);
        Mockito.when(filterFactory.account()).thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, Mockito.RETURNS_DEEP_STUBS));
        Mockito.when(filterFactory.transaction()).thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, Mockito.RETURNS_DEEP_STUBS));
    }
}
