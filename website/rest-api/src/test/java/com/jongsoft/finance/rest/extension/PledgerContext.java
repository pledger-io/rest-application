package com.jongsoft.finance.rest.extension;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.Classifier;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PledgerContext {

    private final List<String> storageTokens;
    private final ApplicationContext applicationContext;

    public PledgerContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.storageTokens = new ArrayList<>();
        applicationContext.registerSingleton(StorageService.class, mock(StorageService.class));
    }

    public PledgerContext withStorage() {
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
              .ifNotPresent(() -> new UserAccount(user, "test123"));
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

        applicationContext.getBean(CurrentUserProvider.class)
              .currentUser()
              .createAccount(name, currency, type);
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

        applicationContext.getBean(CurrentUserProvider.class)
              .currentUser()
              .createCategory(name);
        return this;
    }

    public PledgerContext withTag(String name) {
        var tagProvider = applicationContext.getBean(TagProvider.class);
        if (tagProvider.lookup(name).isPresent()) {
            return this;
        }

        applicationContext.getBean(CurrentUserProvider.class)
              .currentUser()
              .createTag(name);
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

        account.createContract(name, name, startDate, endDate);
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

        account.createSchedule(name, new ScheduleValue(Periodicity.MONTHS, 1), destination, amount);
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
            source.createTransaction(destination, amount, Transaction.Type.CREDIT, builder -> {
                builder.tags(Collections.List(tags));
                builder.date(date);
                builder.currency(source.getCurrency());
                builder.description("Test transaction");
                builder.metadata(metadata);
            }).register();
            return PledgerContext.this;
        }
    }
}
