package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

class AccountResourceTest extends TestSetup {

    private AccountResource subject;

    private SettingProvider settingProvider;
    private CurrentUserProvider currentUserProvider;
    private AccountProvider accountProvider;
    private FilterFactory filterFactory;
    private AccountTypeProvider accountTypeProvider;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        currentUserProvider = Mockito.mock(CurrentUserProvider.class);
        filterFactory = generateFilterMock();
        accountTypeProvider = Mockito.mock(AccountTypeProvider.class);
        settingProvider = Mockito.mock(SettingProvider.class);

        subject = new AccountResource(settingProvider, currentUserProvider, accountProvider, filterFactory, accountTypeProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        var applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void ownAccounts() {
        var resultPage = Mockito.mock(ResultPage.class);
        Mockito.when(resultPage.content()).thenReturn(API.List(
                Account.builder()
                        .id(1L)
                        .name("Sample account")
                        .description("Long description")
                        .iban("NL123INGb23039283")
                        .currency("EUR")
                        .balance(2000.2D)
                        .firstTransaction(LocalDate.of(2019, 1, 1))
                        .lastTransaction(LocalDate.of(2022, 3, 23))
                        .type("checking")
                        .build()));

        Mockito.when(accountTypeProvider.lookup(false)).thenReturn(API.List("default", "savings"));
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        var response = subject.ownAccounts().blockingGet();

        Assertions.assertThat(response).hasSize(1);
        var firstHit = response.get(0);
        Assertions.assertThat(firstHit.getName()).isEqualTo("Sample account");
        Assertions.assertThat(firstHit.getDescription()).isEqualTo("Long description");
        Assertions.assertThat(firstHit.getAccount().getIban()).isEqualTo("NL123INGb23039283");

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockCommand).types(Mockito.eq(API.List("default", "savings")));
    }

    @Test
    void allAccounts() {
        var resultPage = API.List(Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("creditor")
                .build());

        Mockito.when(accountProvider.lookup())
                .thenReturn(resultPage);

        subject.allAccounts().blockingGet();

        Mockito.verify(accountProvider).lookup();
    }

    @Test
    void autocomplete() {
        var resultPage = Mockito.mock(ResultPage.class);
        Mockito.when(resultPage.content()).thenReturn(API.List(
                Account.builder()
                        .id(1L)
                        .name("Sample account")
                        .description("Long description")
                        .iban("NL123INGb23039283")
                        .currency("EUR")
                        .balance(2000.2D)
                        .firstTransaction(LocalDate.of(2019, 1, 1))
                        .lastTransaction(LocalDate.of(2022, 3, 23))
                        .type("checking")
                        .build()));

        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        var response = subject.autocomplete("sampl", "creditor").blockingGet();

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockCommand).name("sampl", false);
        Mockito.verify(mockCommand).types(API.List("creditor"));
    }

    @Test
    void accounts_creditor() {
        var resultPage = ResultPage.of(Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("creditor")
                .build());

        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        subject.accounts(AccountSearchRequest.builder()
                .accountTypes(List.of("creditor"))
                .page(0)
                .build()).blockingGet();

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockCommand).types(API.List("creditor"));
    }

    @Test
    void create() {
        var account = Mockito.spy(Account.builder()
                .id(1L)
                .balance(0D)
                .name("Sample account")
                .type("checking")
                .currency("EUR")
                .build());

        Mockito.when(accountProvider.lookup("Sample account"))
                .thenReturn(Maybe.empty())
                .thenReturn(Maybe.just(account));

        var request = AccountEditRequest.builder()
                .name("Sample account")
                .currency("EUR")
                .type("checking")
                .interest(0.22)
                .interestPeriodicity(Periodicity.MONTHS)
                .build();

        subject.create(request).blockingGet();

        Mockito.verify(accountProvider, Mockito.times(2)).lookup("Sample account");
        Mockito.verify(account).interest(0.22, Periodicity.MONTHS);
    }

}
