package com.jongsoft.finance.extension;

import static org.mockito.Mockito.*;

import com.jongsoft.finance.banking.adapter.api.AccountProvider;
import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.adapter.api.TransactionScheduleProvider;
import com.jongsoft.finance.banking.domain.commands.CreateTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.LinkTransactionCommand;
import com.jongsoft.finance.banking.domain.commands.TagTransactionCommand;
import com.jongsoft.finance.banking.domain.model.*;
import com.jongsoft.finance.banking.domain.model.TransactionCreationHandler;
import com.jongsoft.finance.banking.types.TransactionLinkType;
import com.jongsoft.finance.banking.types.TransactionType;
import com.jongsoft.finance.classification.adapter.api.CategoryProvider;
import com.jongsoft.finance.classification.domain.model.Category;
import com.jongsoft.finance.contract.adapter.api.ContractProvider;
import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.finance.core.adapter.api.CurrentUserProvider;
import com.jongsoft.finance.core.adapter.api.StorageService;
import com.jongsoft.finance.core.adapter.api.UserProvider;
import com.jongsoft.finance.core.domain.AuthenticationFacade;
import com.jongsoft.finance.core.domain.model.UserAccount;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.UserIdentifier;
import com.jongsoft.lang.Control;

import io.micronaut.context.ApplicationContext;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PledgerContext {

    private final List<String> storageTokens;
    private final ApplicationContext applicationContext;

    public PledgerContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.storageTokens = new ArrayList<>();
    }

    public PledgerContext withStorage() {
        applicationContext.registerSingleton(StorageService.class, mock(StorageService.class));
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

    public PledgerContext withUser(String user) {
        applicationContext.getBean(UserProvider.class)
              .lookup(new UserIdentifier(user))
              .ifNotPresent(() -> UserAccount.create(user, "test123"));
        when(applicationContext.getBean(AuthenticationFacade.class).authenticated()).thenReturn(user);
        when(applicationContext.getBean(CurrentUserProvider.class).currentUser())
              .thenAnswer(_ ->
                    applicationContext.getBean(UserProvider.class)
                          .lookup(new UserIdentifier(user))
                          .getOrThrow(() -> new RuntimeException("Cannot find user " + user)));
        return this;
    }

    public PledgerContext withBankAccount(String name, String currency, String type) {
        var accountProvider = applicationContext.getBean(AccountProvider.class);
        if (accountProvider.lookup(name).isPresent()) {
            return this;
        }

        Account.create(applicationContext.getBean(CurrentUserProvider.class).currentUser().getUsername(), name, currency, type);
        return this;
    }

    public PledgerContext withCreditor(String name, String currency) {
        return withBankAccount(name, currency, "creditor");
    }

    public PledgerContext withDebtor(String name, String currency) {
        return withBankAccount(name, currency, "debtor");
    }

    public PledgerContext withCategory(String name) {
        var categoryProvider = applicationContext.getBean(CategoryProvider.class);
        if (categoryProvider.lookup(name).isPresent()) {
            return this;
        }

        Category.create(name, "");
        return this;
    }

    public PledgerContext withTag(String name) {
        var tagProvider = applicationContext.getBean(TagProvider.class);
        if (tagProvider.lookup(name).isPresent()) {
            return this;
        }

        Tag.create(name);
        return this;
    }

    public PledgerContext withContract(String company, String name, LocalDate startDate, LocalDate endDate) {
        var contractProvider = applicationContext.getBean(ContractProvider.class);
        if (contractProvider.lookup(name).isPresent()) {
            return this;
        }

        var account = applicationContext.getBean(AccountProvider.class)
              .lookup(company)
              .getOrThrow(() -> new RuntimeException("Cannot find account " + company));

        Contract.create(account, name, name, startDate, endDate);
        if (endDate.isBefore(LocalDate.now())) {
            contractProvider.lookup(name)
                  .getOrThrow(() -> new RuntimeException("Cannot find contract " + name))
                  .terminate();
        }
        return this;
    }

    public PledgerContext withSchedule(String source, String company, String name, double amount, LocalDate startDate, LocalDate endDate) {
        var account = applicationContext.getBean(AccountProvider.class).lookup(source)
              .getOrThrow(() -> new RuntimeException("Cannot find account " + source));
        var destination = applicationContext.getBean(AccountProvider.class).lookup(company)
              .getOrThrow(() -> new RuntimeException("Cannot find account " + company));

        TransactionSchedule.create(name, new ScheduleValue(Periodicity.MONTHS, 1), account, destination, amount);
        var schedule = applicationContext.getBean(TransactionScheduleProvider.class)
              .lookup().first(s -> s.getName().equals(name)).getOrThrow(() -> new RuntimeException("Cannot find schedule " + name));
        schedule.limit(startDate, endDate);

        return this;
    }

    public TransactionContext withTransaction(String source, String target, double amount) {
        var account = applicationContext.getBean(AccountProvider.class).lookup(source)
            .getOrThrow(() -> new RuntimeException("Cannot find account " + source));
        var destination = applicationContext.getBean(AccountProvider.class).lookup(target)
            .getOrThrow(() -> new RuntimeException("Cannot find account " + target));

        return new TransactionContext(account, destination, amount);
    }

    void reset() {

    }

    public class TransactionContext {

        private final Account source;
        private final Account destination;
        private final double amount;
        private List<String> tags;
        private LocalDate date;
        private Map<String, Classifier> metadata;

        TransactionContext(Account source, Account destination, double amount) {
            this.source = source;
            this.destination = destination;
            this.amount = amount;
            this.metadata = new HashMap<>();
        }

        public TransactionContext withTags(String... tags) {
            this.tags = List.of(tags);
            return this;
        }

        public TransactionContext withCategory(String category) {
            var categoryProvider = applicationContext.getBean(CategoryProvider.class);
            var entity = categoryProvider.lookup(category).getOrThrow(() -> new RuntimeException("Cannot find category " + category));
            metadata.put("CATEGORY", entity);
            return this;
        }

        public TransactionContext on(LocalDate date) {
            this.date = date;
            return this;
        }

        public PledgerContext upsert() {
            var command = new CreateTransactionCommand(
                date, "Test transaction",
                TransactionType.CREDIT,
                null,
                source.getCurrency(),
                source.getId(),
                destination.getId(),
                BigDecimal.valueOf(amount));
            var id = applicationContext.getBean(TransactionCreationHandler.class).handleCreatedEvent(command);

            LinkTransactionCommand.linkCreated(id, TransactionLinkType.CATEGORY, metadata.get("CATEGORY").getId());
            TagTransactionCommand.tagCreated(id, com.jongsoft.lang.Collections.List(tags));

            return PledgerContext.this;
        }
    }
}
