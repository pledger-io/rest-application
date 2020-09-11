package com.jongsoft.finance.rest.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.lang.API;

class BalanceResourceTest {

    private BalanceResource subject;

    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;

    @BeforeEach
    void setup() {
        filterFactory = generateMock();
        transactionProvider = Mockito.mock(TransactionProvider.class);
        subject = new BalanceResource(filterFactory, transactionProvider);

        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());
    }

    @Test
    void calculate() {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(DateRange.forMonth(2019, 1));

        subject.calculate(request).blockingGet();

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).balance(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
    }

    private FilterFactory generateMock() {
        final FilterFactory filterFactory = Mockito.mock(FilterFactory.class);
        Mockito.when(filterFactory.transaction())
                .thenReturn(Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.account())
                .thenReturn(Mockito.mock(AccountProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.expense())
                .thenReturn(Mockito.mock(ExpenseProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.category())
                .thenReturn(Mockito.mock(CategoryProvider.FilterCommand.class, InvocationOnMock::getMock));
        Mockito.when(filterFactory.tag())
                .thenReturn(Mockito.mock(TagProvider.FilterCommand.class, InvocationOnMock::getMock));
        return filterFactory;
    }

}
