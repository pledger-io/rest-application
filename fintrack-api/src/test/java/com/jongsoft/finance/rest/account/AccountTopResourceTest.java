package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.time.LocalDate;

class AccountTopResourceTest extends TestSetup {

    private AccountTopResource subject;

    private AccountProvider accountProvider;
    private FilterFactory filterFactory;
    private SettingProvider settingProvider;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        filterFactory = generateFilterMock();
        settingProvider = Mockito.mock(SettingProvider.class);

        subject = new AccountTopResource(accountProvider, filterFactory, settingProvider);
    }

    @Test
    void topDebtors() {
        Account account = Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("checking")
                .build();

        Mockito.doReturn(Collections.List(new AccountProvider.AccountSpending() {
            @Override
            public Account account() {
                return account;
            }

            @Override
            public double total() {
                return 1200D;
            }

            @Override
            public double average() {
                return 50D;
            }
        })).when(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(true));


        StepVerifier.create(subject.topDebtors(DateUtils.startOfMonth(2019, 1), DateUtils.startOfMonth(2019, 2)))
                .expectNextCount(1)
                .verifyComplete();

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(true));
        Mockito.verify(mockCommand).types(Collections.List("debtor"));
    }

    @Test
    void topCreditor() {
        Account account = Account.builder()
                .id(1L)
                .name("Sample account")
                .description("Long description")
                .iban("NL123INGb23039283")
                .currency("EUR")
                .balance(2000.2D)
                .firstTransaction(LocalDate.of(2019, 1, 1))
                .lastTransaction(LocalDate.of(2022, 3, 23))
                .type("checking")
                .build();

        Mockito.doReturn(Collections.List(new AccountProvider.AccountSpending() {
            @Override
            public Account account() {
                return account;
            }

            @Override
            public double total() {
                return 1200D;
            }

            @Override
            public double average() {
                return 50D;
            }
        })).when(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(false));

        StepVerifier.create(subject.topCreditor(DateUtils.startOfMonth(2019, 1), DateUtils.startOfMonth(2019, 2)))
                .expectNextCount(1)
                .verifyComplete();

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(
                Mockito.any(AccountProvider.FilterCommand.class),
                Mockito.eq(DateUtils.forMonth(2019, 1)),
                Mockito.eq(false));
        Mockito.verify(mockCommand).types(Collections.List("creditor"));
    }

}
