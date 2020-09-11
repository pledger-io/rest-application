package com.jongsoft.finance.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.events.AccountCreatedEvent;
import com.jongsoft.finance.domain.importer.BatchImportConfig;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import com.jongsoft.finance.domain.transaction.events.TagCreatedEvent;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.domain.user.events.UserAccountMultiFactorEvent;
import com.jongsoft.finance.domain.user.events.UserAccountPasswordChangedEvent;
import com.jongsoft.finance.domain.user.events.UserAccountSettingEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.API;

import io.micronaut.context.event.ApplicationEventPublisher;

class UserAccountTest {

    private UserAccount fullAccount;
    private UserAccount readOnlyAccount;

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        fullAccount = UserAccount.builder()
                .id(1L)
                .username("demo-user")
                .password("1234567")
                .roles(API.List(new Role("accountant")))
                .build();
        readOnlyAccount = UserAccount.builder()
                .id(2L)
                .username("demo-user")
                .password("1234567")
                .roles(API.List(new Role("reader")))
                .build();
    }

    @Test
    void createAccount() {
        ArgumentCaptor<AccountCreatedEvent> changeCaptor = ArgumentCaptor.forClass(AccountCreatedEvent.class);

        Account account = fullAccount.createAccount("Demo account", "EUR", "checking");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(account.getUser()).isEqualTo(fullAccount);
        assertThat(account.getName()).isEqualTo("Demo account");
        assertThat(account.getCurrency()).isEqualTo("EUR");
        assertThat(account.getType()).isEqualTo("checking");
    }

    @Test
    void createAccount_NotAllowed() {
        ArgumentCaptor<AccountCreatedEvent> changeCaptor = ArgumentCaptor.forClass(AccountCreatedEvent.class);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> readOnlyAccount.createAccount("Demo account", "EUR", "checking"));

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(changeCaptor.capture());

        assertThat(thrown.getMessage()).isEqualTo("User cannot create accounts, incorrect privileges.");
    }

    @Test
    void createTag() {
        var tag = fullAccount.createTag("Tag 1");

        ArgumentCaptor<TagCreatedEvent> changeCaptor = ArgumentCaptor.forClass(TagCreatedEvent.class);
        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(tag.name()).isEqualTo("Tag 1");
        assertThat(changeCaptor.getValue().getTag()).isEqualTo("Tag 1");
        assertThat(changeCaptor.getValue().getUser()).isEqualTo(fullAccount);
    }

    @Test
    void createTag_NotAllowed() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> readOnlyAccount.createTag("Tag 1"));

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any());

        assertThat(thrown.getMessage()).isEqualTo("User cannot create tags, incorrect privileges.");
    }

    @Test
    void createRule() {
        final TransactionRule rule = fullAccount.createRule("Checking rule", true);

        assertThat(rule.getName()).isEqualTo("Checking rule");
        assertThat(rule.isRestrictive()).isTrue();
    }

    @Test
    void createCategory() {
        Category category = fullAccount.createCategory("demo-cat");
        assertThat(category.getUser()).isEqualTo(fullAccount);
        assertThat(category.getLabel()).isEqualTo("demo-cat");
        assertThat(category.getId()).isNull();
    }

    @Test
    void createCategory_NotAllowed() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> readOnlyAccount.createCategory("demo-cat"));

        assertThat(thrown.getMessage()).isEqualTo("User cannot create categories, incorrect privileges.");
    }

    @Test
    void createBatchConfiguration() {
        final BatchImportConfig configuration = fullAccount.createImportConfiguration("test-config", "file-code-sample");

        assertThat(configuration.getName()).isEqualTo("test-config");
        assertThat(configuration.getFileCode()).isEqualTo("file-code-sample");
        assertThat(configuration.getUser()).isEqualTo(fullAccount);
    }

    @Test
    void createBatchConfiguration_NotAllowed() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> readOnlyAccount.createImportConfiguration("test-config", "file-code-sample"));

        assertThat(thrown.getMessage()).isEqualTo("User cannot create import configuration, incorrect privileges.");
    }

    @Test
    void createBudget_NotAllowed() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> readOnlyAccount.createBudget(LocalDate.of(2019, 1, 1), 2500));

        assertThat(thrown.getMessage()).isEqualTo("User cannot create budgets, incorrect privileges.");
    }

    @Test
    void createBudget() {
        ArgumentCaptor<BudgetCreatedEvent> captor = ArgumentCaptor.forClass(BudgetCreatedEvent.class);

        Budget budget = fullAccount.createBudget(LocalDate.of(2019, 1, 1), 2500);

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(budget.getStart()).isEqualTo(LocalDate.of(2019, 1, 1));
        assertThat(budget.getExpectedIncome()).isEqualTo(2500);
    }

    @Test
    void changePassword() {
        ArgumentCaptor<UserAccountPasswordChangedEvent> changeCaptor = ArgumentCaptor.forClass(UserAccountPasswordChangedEvent.class);

        fullAccount.changePassword("update1234");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().getUsername()).isEqualTo(fullAccount.getUsername());
    }

    @Test
    void changeCurrency() {
        ArgumentCaptor<UserAccountSettingEvent> changeCaptor = ArgumentCaptor.forClass(UserAccountSettingEvent.class);

        fullAccount.changeCurrency(Currency.getInstance("EUR"));

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().getUsername()).isEqualTo(fullAccount.getUsername());
        assertThat(changeCaptor.getValue().getValue()).isEqualTo("EUR");
    }

    @Test
    void changeTheme() {
        ArgumentCaptor<UserAccountSettingEvent> changeCaptor = ArgumentCaptor.forClass(UserAccountSettingEvent.class);

        fullAccount.changeTheme("light");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().getUsername()).isEqualTo(fullAccount.getUsername());
        assertThat(changeCaptor.getValue().getValue()).isEqualTo("light");
        assertThat(changeCaptor.getValue().getTypeOfSetting()).isEqualTo(UserAccountSettingEvent.Type.THEME);
    }

    @Test
    void enableMultiFactorAuthentication() {
        ArgumentCaptor<UserAccountMultiFactorEvent> changeCaptor = ArgumentCaptor.forClass(UserAccountMultiFactorEvent.class);

        fullAccount.enableMultiFactorAuthentication();
        // second attempt should not raise an event
        fullAccount.enableMultiFactorAuthentication();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().isEnabled()).isTrue();
        assertThat(changeCaptor.getValue().getUsername()).isEqualTo("demo-user");
    }

    @Test
    void disableMultiFactorAuthentication() {
        ArgumentCaptor<UserAccountMultiFactorEvent> changeCaptor = ArgumentCaptor.forClass(UserAccountMultiFactorEvent.class);

        fullAccount.enableMultiFactorAuthentication();
        // second attempt should not raise an event
        fullAccount.disableMultiFactorAuthentication();

        Mockito.verify(applicationEventPublisher, Mockito.times(2)).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getAllValues().get(1).isEnabled()).isFalse();
        assertThat(changeCaptor.getAllValues().get(1).getUsername()).isEqualTo("demo-user");
    }
}
