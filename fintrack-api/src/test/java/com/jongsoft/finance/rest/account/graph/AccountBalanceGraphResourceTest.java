package com.jongsoft.finance.rest.account.graph;

import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.API;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.reactivex.Maybe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Locale;

class AccountBalanceGraphResourceTest extends TestSetup {

    private AccountBalanceGraphResource subject;

    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;
    private AccountProvider accountProvider;
    private CurrencyProvider currencyProvider;

    @BeforeEach
    void setup() {
        filterFactory = generateFilterMock();
        transactionProvider = Mockito.mock(TransactionProvider.class);
        accountProvider = Mockito.mock(AccountProvider.class);
        currencyProvider = Mockito.mock(CurrencyProvider.class);
        var messageSource = new ResourceBundleMessageSource("i18n.messages");

        subject = new AccountBalanceGraphResource(
                messageSource,
                filterFactory,
                accountProvider,
                transactionProvider,
                currencyProvider);
    }

    @Test
    void balance() {
        final Account account = Account.builder()
                .id(123L)
                .currency("EUR")
                .build();

        Mockito.when(currencyProvider.lookup(Mockito.anyString())).thenReturn(Maybe.empty());
        Mockito.when(accountProvider.lookup(123L)).thenReturn(API.Option(account));
        Mockito.when(transactionProvider.balance(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(API.Option())
                .thenReturn(API.Option(25.44D));
        Mockito.when(transactionProvider.daily(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(API.List(new TransactionProvider.DailySummary() {
                    @Override
                    public LocalDate day() {
                        return LocalDate.of(2019, 1, 3);
                    }

                    @Override
                    public double summary() {
                        return 25.44;
                    }
                }));

         var response = subject.balance(
                 123L,
                 Dates.startOfMonth(2019, 1),
                 Dates.endOfMonth(2019, 1),
                 Locale.GERMAN);

        Mockito.verify(accountProvider).lookup(123L);
        Mockito.verify(transactionProvider, Mockito.times(2)).balance(Mockito.any(TransactionProvider.FilterCommand.class));
    }
}