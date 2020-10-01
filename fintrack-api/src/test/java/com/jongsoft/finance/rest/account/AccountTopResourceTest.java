package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        Mockito.doReturn(API.List(new AccountProvider.AccountSpending() {
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
                Mockito.eq(DateRange.forMonth(2019, 1)));


        subject.topDebtors(Dates.startOfMonth(2019, 1), Dates.endOfMonth(2019, 1))
                .test()
                .assertComplete()
                .assertValueCount(1);

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(Mockito.any(AccountProvider.FilterCommand.class), Mockito.eq(DateRange.forMonth(2019, 1)));
        Mockito.verify(mockCommand).types(API.List("debtor"));
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

        Mockito.doReturn(API.List(new AccountProvider.AccountSpending() {
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
                Mockito.eq(DateRange.forMonth(2019, 1)));

        subject.topCreditor(Dates.startOfMonth(2019, 1), Dates.endOfMonth(2019, 1))
                .test()
                .assertComplete()
                .assertValueCount(1);

        var mockCommand = filterFactory.account();
        Mockito.verify(accountProvider).top(Mockito.any(AccountProvider.FilterCommand.class), Mockito.eq(DateRange.forMonth(2019, 1)));
        Mockito.verify(mockCommand).types(API.List("creditor"));
    }

}