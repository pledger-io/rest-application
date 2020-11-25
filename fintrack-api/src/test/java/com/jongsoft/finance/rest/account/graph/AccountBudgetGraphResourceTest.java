package com.jongsoft.finance.rest.account.graph;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.core.date.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.CurrencyProvider;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;

public class AccountBudgetGraphResourceTest extends TestSetup {

    private AccountBudgetGraphResource subject;

    private AccountProvider accountProvider;
    private TransactionProvider transactionService;
    private BudgetProvider budgetService;
    private FilterFactory filterFactory;
    private CurrencyProvider currencyProvider;

    @BeforeEach
    void setup() {
        filterFactory = generateFilterMock();
        transactionService = Mockito.mock(TransactionProvider.class);
        accountProvider = Mockito.mock(AccountProvider.class);
        budgetService = Mockito.mock(BudgetProvider.class);
        currencyProvider = Mockito.mock(CurrencyProvider.class);
        var messageSource = new ResourceBundleMessageSource("i18n.messages");

        subject = new AccountBudgetGraphResource(
                messageSource,
                filterFactory,
                accountProvider,
                transactionService,
                budgetService,
                currencyProvider);
    }

    @Test
    void graph() throws Exception {
        var account = Account.builder()
                .id(123L)
                .currency("EUR")
                .user(ACTIVE_USER)
                .build();

        var expense = Budget.Expense.builder()
                .id(1L)
                .name("Demo Expense")
                .build();

        Mockito.when(currencyProvider.lookup(Mockito.anyString())).thenReturn(Maybe.empty());
        Mockito.when(transactionService.balance(Mockito.any())).thenReturn(Control.Option());
        Mockito.when(accountProvider.lookup(123L)).thenReturn(Control.Option(account));
        Mockito.when(budgetService.lookup(2019, 1)).thenReturn(
                Single.just(Budget.builder()
                        .expenses(Collections.List(expense))
                        .build()));

        subject.budget(
                123L,
                DateUtils.startOfMonth(2019, 1),
                DateUtils.endOfMonth(2019, 1),
                Locale.GERMAN);

        var filterCommand = filterFactory.transaction();
        Mockito.verify(accountProvider).lookup(123L);
        Mockito.verify(filterCommand).accounts(Collections.List(new EntityRef(account.getId())));
        Mockito.verify(filterCommand).onlyIncome(false);
        Mockito.verify(filterCommand).range(DateRangeOld.forMonth(2019, 1));

        Mockito.verify(transactionService, Mockito.times(2)).balance(Mockito.any());
    }

}
