package com.jongsoft.finance.rest.extension;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        return this;
    }

    void reset() {

    }
}
