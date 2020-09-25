package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        Mockito.when(accountProvider.lookup(Mockito.anyLong())).thenReturn(API.Option());

        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void get_missing() {
        org.junit.jupiter.api.Assertions.assertThrows(StatusException.class,
                () -> subject.get(1L).blockingGet());
    }

    @Test
    void get() {
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(API.Option(Account.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .balance(0D)
                        .name("Sample account")
                        .currency("EUR")
                        .build()));

        var response = subject.get(123L).blockingGet();

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    void update_missing() {
        var response = subject.update(1L, new AccountEditRequest()).blockingGet();

        Mockito.verify(accountProvider).lookup(1L);
        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    void update() {
        Mockito.when(accountProvider.lookup(123L))
                .thenReturn(API.Option(Account.builder()
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

        var response = subject.update(123L, request).blockingGet();

        Mockito.verify(accountProvider).lookup(123L);
        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.OK.getCode());
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
                .thenReturn(API.Option(account));

        var response = subject.delete(123L).blockingGet();

        Assertions.assertThat(response.code()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

        Mockito.verify(account).terminate();
    }
}