package com.jongsoft.finance.bpmn.process;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.SystemAccountTypes;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.importer.BatchImport;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.reflect.ReflectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.camunda.bpm.engine.ProcessEngine;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class RuntimeContext {

    private static final Logger log = LoggerFactory.getLogger(RuntimeContext.class);

    private ApplicationContext applicationContext;
    private ApplicationEventPublisher applicationEventPublisher;

    private ProcessEngine processEngine;

    private UserAccount userAccount;

    private final List<String> storageTokens;
    private final MutableLong idGenerator;
    private final List<Budget> registeredBudgets;

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
        this.applicationEventPublisher = Mockito.spy(applicationContext.getBean(ApplicationEventPublisher.class));
        idGenerator = new MutableLong(100);
        registeredBudgets = new ArrayList<>();

        setupDefaultMocks();
    }

    void clean() {
        processEngine.close();
        storageTokens.clear();
        registeredBudgets.clear();
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

    @SuppressWarnings("unchecked")
    public RuntimeContext withBudgets() {
        var budgetProvider = applicationContext.getBean(BudgetProvider.class);
        Mockito.when(budgetProvider.lookup(Mockito.anyInt(), Mockito.anyInt()))
                .thenAnswer(invocation -> {
                    var date = LocalDate.of(invocation.getArgument(0), invocation.getArgument(1, Integer.class), 1);

                    return Control.Option(registeredBudgets.stream()
                            .filter(budget -> budget.getStart().isBefore(date) || budget.getStart().isEqual(date))
                            .max(Comparator.comparing(Budget::getStart))
                            .orElse(null));
                });

        Mockito.doAnswer((Answer<Void>) invocation -> {
                    var createCommand = invocation.getArgument(0, CreateBudgetCommand.class);

                    var budget = Budget.builder()
                            .start(createCommand.budget().getStart())
                            .expectedIncome(createCommand.budget().getExpectedIncome())
                            .id(idGenerator.getAndIncrement())
                            .expenses(Collections.List())
                            .build();
                    registeredBudgets.add(budget);
                    return null;
                }).when(applicationEventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
        return this;
    }

    public RuntimeContext withBudget(int year, int month, Budget budget) {
        var budgetProvider = applicationContext.getBean(BudgetProvider.class);
        Mockito.when(budgetProvider.lookup(year, month))
                .thenReturn(Control.Option(budget));
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

    public RuntimeContext withTransactionSchedule(ScheduledTransaction scheduledTransaction) {
        var transactionProvider = applicationContext.getBean(TransactionScheduleProvider.class);
        Mockito.when(transactionProvider.lookup(scheduledTransaction.getId()))
                .thenReturn(Control.Option(scheduledTransaction));
        return this;
    }

    public OngoingStubbing<ResultPage<Transaction>> withTransactionPages() {
        var transactionProvider = applicationContext.getBean(TransactionProvider.class);
        return Mockito.when(transactionProvider.lookup(Mockito.any(TransactionProvider.FilterCommand.class)));
    }

    public OngoingStubbing<Optional<BigDecimal>> withBalance() {
        var transactionProvider = applicationContext.getBean(TransactionProvider.class);
        return Mockito.when(transactionProvider.balance(Mockito.any(TransactionProvider.FilterCommand.class)));
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

    public RuntimeContext withReconcileAccount() {
        var accountProvider = applicationContext.getBean(AccountProvider.class);
        var reconcileAccount = Account.builder().id(99L)
                .type("reconcile")
                .name("Reconcile account")
                .build();
        Mockito.when(accountProvider.lookup(SystemAccountTypes.RECONCILE))
                .thenReturn(Control.Option(reconcileAccount));
        Mockito.when(accountProvider.lookup(reconcileAccount.getId()))
                .thenReturn(Control.Option(reconcileAccount));
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

    public RuntimeContext withContract(Contract contract) {
        Mockito.when(applicationContext.getBean(ContractProvider.class).lookup(contract.getId()))
                .thenReturn(Control.Option(contract));
        return this;
    }

    public RunningProcessExecution execute(String processKey, Map<String, Object> variables) {
        return new RunningProcessExecution(processEngine, processKey, variables);
    }

    public RuntimeContext verifyAccountCreated(String name, String currency, String type) {
            Mockito.verify(userAccount, Mockito.atMostOnce())
                    .createAccount(name, currency, type);
        return this;
    }

    /**
     * Verifies if a category with the specified name has been created.
     *
     * @param name the name of the category to be verified
     * @return the current RuntimeContext instance
     */
    public RuntimeContext verifyCategoryCreated(String name) {
        Mockito.verify(userAccount, Mockito.atMostOnce())
                .createCategory(name);
        return this;
    }

    /**
     * Verifies the transactions by performing validations provided by the consumer.
     *
     * @param validations the consumer that accepts a ListAssert<Transaction> for performing validations
     * @return the RuntimeContext instance
     */
    public RuntimeContext verifyTransactions(Consumer<ListAssert<Transaction>> validations) {
        var createdTransactions = Mockito.mockingDetails(applicationContext.getBean(TransactionCreationHandler.class)).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("handleCreatedEvent"))
                .map(invocation -> invocation.getArgument(0, CreateTransactionCommand.class))
                .map(CreateTransactionCommand::transaction)
                .toList();

        validations.accept(Assertions.assertThat(createdTransactions));

        return this;
    }

    /**
     * Verifies that all storage tokens have been cleaned.
     *
     * @return The RuntimeContext object.
     */
    public RuntimeContext verifyStorageCleaned() {
        storageTokens.forEach(token ->
                Mockito.verify(applicationContext.getBean(StorageService.class)).remove(token));
        return this;
    }

    /**
     * Verifies the budget for the specified start date.
     *
     * @param startDate the start date of the budget to verify
     * @param validations the consumer function that performs assertions on the budget
     * @return the current RuntimeContext object
     */
    public RuntimeContext verifyBudget(LocalDate startDate, Consumer<ObjectAssert<Budget>> validations) {
        validations.accept(
                Assertions.assertThat(registeredBudgets.stream()
                                .filter(budget -> budget.getStart().equals(startDate))
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("Budget not found for start date: " + startDate)))
                        .as("Budget created for start date: " + startDate));
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

    void resetMocks() {
        Mockito.reset(
                applicationContext.getBean(BudgetProvider.class),
                applicationContext.getBean(AuthenticationFacade.class),
                applicationContext.getBean(CurrentUserProvider.class),
                applicationContext.getBean(AccountProvider.class),
                applicationContext.getBean(TransactionProvider.class),
                applicationContext.getBean(ImportProvider.class),
                applicationContext.getBean(TransactionCreationHandler.class),
                applicationContext.getBean(TransactionRuleProvider.class),
                applicationContext.getBean(CategoryProvider.class),
                applicationContext.getBean(StorageService.class),
                applicationContext.getBean(TransactionScheduleProvider.class),
                userAccount);

        setupDefaultMocks();
    }

    private void setupDefaultMocks() {
        Mockito.when(applicationContext.getBean(AuthenticationFacade.class).authenticated())
                .thenReturn("test-user");
        Mockito.when(applicationContext.getBean(CurrentUserProvider.class).currentUser())
                .thenReturn(userAccount);

        new EventBus(applicationEventPublisher);

        // Prepare the mocks for the filter factory
        var filterFactory = applicationContext.getBean(FilterFactory.class);
        Mockito.when(filterFactory.account()).thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, Mockito.RETURNS_DEEP_STUBS));
        Mockito.when(filterFactory.transaction()).thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, Mockito.RETURNS_DEEP_STUBS));
    }
}
