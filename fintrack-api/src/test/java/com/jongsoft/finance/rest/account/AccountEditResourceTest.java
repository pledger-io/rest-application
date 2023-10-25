package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountEditResourceTest extends TestSetup {

    private AccountEditResource subject;

    private CurrentUserProvider currentUserProvider;
    private AccountProvider accountProvider;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        currentUserProvider = Mockito.mock(CurrentUserProvider.class);

        subject = new AccountEditResource(currentUserProvider, accountProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        Mockito.when(accountProvider.lookup(Mockito.anyLong())).thenReturn(Control.Option());

        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void get_missing() {
        Assertions.assertThrows(
                StatusException.class,
                () -> subject.get(1L),
                "Account not found");
    }

    @Test
    void get() {
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(Account.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .balance(0D)
                        .name("Sample account")
                        .currency("EUR")
                        .build()));

        assertThat(subject.get(123L))
                .isNotNull()
                .isInstanceOf(AccountResponse.class)
                .hasFieldOrPropertyWithValue("name", "Sample account");

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    void update_missing() {
        Assertions.assertThrows(
                StatusException.class,
                () -> subject.update(1L, new AccountEditRequest()),
                "Account not found");

        Mockito.verify(accountProvider).lookup(1L);
    }

    @Test
    void update() {
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(Account.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .balance(0D)
                        .name("Sample account")
                        .currency("EUR")
                        .build()));

        var request = AccountEditRequest.builder()
                .name("Sample account")
                .currency("EUR")
                .type("checking")
                .build();

        assertThat(subject.update(123L, request))
                .isNotNull()
                .isInstanceOf(AccountResponse.class)
                .hasFieldOrPropertyWithValue("name", "Sample account");

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    void updateIcon() {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .build());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

        var response = subject.persistImage(1L, new AccountImageRequest("file-code"));

        assertThat(response.getIconFileCode()).isEqualTo("file-code");
        Mockito.verify(account).registerIcon("file-code");
    }

    @Test
    void delete() {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .build());
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(account));

        subject.delete(123L);

        Mockito.verify(account).terminate();
    }

    @Test
    void createSavingGoal() {
        Account account = Mockito.spy(Account.builder()
                .id(1L)
                .user(ACTIVE_USER)
                .balance(0D)
                .name("Sample account")
                .currency("EUR")
                .type("savings")
                .build());

        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(Control.Option(account));

        subject.createSavingGoal(
                123L,
                AccountSavingGoalCreateRequest.builder()
                        .goal(BigDecimal.valueOf(1500))
                        .targetDate(LocalDate.now().plusDays(300))
                        .name("Saving for washer")
                        .build());

        Mockito.verify(account).createSavingGoal(
                "Saving for washer",
                BigDecimal.valueOf(1500),
                LocalDate.now().plusDays(300));
    }
}
