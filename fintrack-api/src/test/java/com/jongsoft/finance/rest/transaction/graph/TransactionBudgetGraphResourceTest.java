package com.jongsoft.finance.rest.transaction.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.AccountTypeProvider;
import com.jongsoft.finance.domain.core.Currency;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.API;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;

class TransactionBudgetGraphResourceTest extends TestSetup {

    private TransactionBudgetGraphResource subject;

    private FilterFactory filterFactory;

    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private BudgetProvider budgetProvider;
    @Mock
    private AccountProvider accountProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private AccountTypeProvider accountTypeProvider;
    @Mock
    private CurrencyProvider currencyProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterFactory = generateFilterMock();

        subject = new TransactionBudgetGraphResource(
                filterFactory,
                transactionProvider,
                budgetProvider,
                accountProvider,
                currentUserProvider,
                new ResourceBundleMessageSource("i18n.messages"),
                accountTypeProvider,
                currencyProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
        Mockito.when(currencyProvider.lookup("EUR")).thenReturn(
                Maybe.just(Currency.builder()
                        .symbol('E')
                        .build()));
    }

    @Test
    void budget() {
        final Account account = Account.builder()
                .id(123L)
                .currency("EUR")
                .build();

        Budget.Expense expense = Budget.Expense.builder()
                .id(1L)
                .name("Demo Expense")
                .build();

        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of(account));
        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(Single.just(Budget.builder()
                .expenses(API.List(expense))
                .build()));

        subject.budget(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 1, 31),
                Locale.GERMAN);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
        Mockito.verify(mockFilter).accounts(API.List(new EntityRef(123L)));
        Mockito.verify(mockFilter).expenses(API.List(new EntityRef(1L)));
    }
}