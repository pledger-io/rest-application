package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

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
        StepVerifier.create(subject.get(1L))
                .verifyErrorMessage("Account not found");
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

        StepVerifier.create(subject.get(123L))
                .assertNext(response -> {
                    assertThat(response.getName()).isEqualTo("Sample account");
                })
                .verifyComplete();

        Mockito.verify(accountProvider).lookup(123L);
    }

    @Test
    void update_missing() {
        StepVerifier.create(subject.update(1L, new AccountEditRequest()))
                .verifyErrorMessage("No account found with id 1");

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

        StepVerifier.create(subject.update(123L, request))
                .assertNext(response -> {
                    assertThat(response.getName()).isEqualTo("Sample account");
                })
                .verifyComplete();

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

        var response = subject.persistImage(1L, new AccountImageRequest("file-code"))
                .block();

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
}
