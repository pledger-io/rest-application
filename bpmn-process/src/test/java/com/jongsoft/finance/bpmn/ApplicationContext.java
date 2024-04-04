package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.core.DataSourceMigration;
import com.jongsoft.finance.core.MailDaemon;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.rule.RuleDataSet;
import com.jongsoft.finance.rule.RuleEngine;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

import java.util.function.Consumer;

@Factory
public class ApplicationContext {

    @Singleton
    Consumer<CreateAccountCommand> accountCreationListener(AccountProvider accountProvider) {
        return new Consumer<>() {
            @Override
            @EventListener
            public void accept(CreateAccountCommand accountCreatedEvent) {
                Mockito.when(accountProvider.lookup(accountCreatedEvent.name()))
                        .thenReturn(Control.Option(Account.builder()
                                .name(accountCreatedEvent.name())
                                .currency(accountCreatedEvent.currency())
                                .type(accountCreatedEvent.type())
                                .build()));
            }
        };
    }

    @Singleton
    public CurrentUserProvider currentUserProvider() {
        return Mockito.mock(CurrentUserProvider.class);
    }

    @Singleton
    public AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

    @Singleton
    TagProvider tagProvider() {
        return Mockito.mock(TagProvider.class);
    }

    @Singleton
    TransactionCreationHandler transactionCreationHandler() {
        return Mockito.mock(TransactionCreationHandler.class);
    }

    @Singleton
    UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }

    @Singleton
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @Singleton
    FilterFactory accountFilterFactory() {
        return Mockito.mock(FilterFactory.class);
    }

    @Singleton
    SettingProvider applicationSettings() {
        return Mockito.mock(SettingProvider.class);
    }

    @Singleton
    CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @Singleton
    TransactionRuleProvider transactionRuleProvider() {
        return Mockito.mock(TransactionRuleProvider.class);
    }

    @Singleton
    StorageService storageService() {
        return Mockito.spy(StorageService.class);
    }

    @Singleton
    ContractProvider contractProvider() {
        return Mockito.mock(ContractProvider.class);
    }

    @Singleton
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @Singleton
    ExpenseProvider expenseProvider() {
        return Mockito.mock(ExpenseProvider.class);
    }

    @Singleton
    RuleEngine ruleEngine() {
        final RuleEngine mock = Mockito.mock(RuleEngine.class);
        Mockito.when(mock.run(Mockito.any())).thenReturn(new RuleDataSet());
        Mockito.when(mock.run(Mockito.any(), Mockito.any())).thenReturn(new RuleDataSet());
        return mock;
    }

    @Singleton
    TransactionRuleGroupProvider transactionRuleGroupProvider() {
        return Mockito.mock(TransactionRuleGroupProvider.class);
    }

    @Singleton
    TransactionScheduleProvider transactionScheduleProvider() {
        return Mockito.mock(TransactionScheduleProvider.class);
    }

    @Singleton
    ApplicationEventPublisher applicationEventPublisher() {
        final ApplicationEventPublisher mock = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(mock);
        return mock;
    }

    @Singleton
    ImportProvider importProvider() {
        return Mockito.mock(ImportProvider.class);
    }

    @Singleton
    ImportConfigurationProvider csvConfigProvider() {
        return Mockito.mock(ImportConfigurationProvider.class);
    }

    @Singleton
    TransactionProvider transactionService() {
        return Mockito.mock(TransactionProvider.class);
    }

    @Singleton
    MailDaemon mailDaemon() {
        return Mockito.mock(MailDaemon.class);
    }

    @Singleton
    DataSourceMigration dataSourceMigration() {
        return Mockito.mock(DataSourceMigration.class);
    }

}
