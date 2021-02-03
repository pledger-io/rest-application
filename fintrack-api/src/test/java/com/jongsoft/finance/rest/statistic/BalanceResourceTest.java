package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Collection;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

class BalanceResourceTest extends TestSetup {

    private BalanceResource subject;

    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;
    private ApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        applicationContext = Mockito.mock(ApplicationContext.class);
        filterFactory = generateFilterMock();
        transactionProvider = Mockito.mock(TransactionProvider.class);
        subject = new BalanceResource(filterFactory, transactionProvider, applicationContext);

        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());
        Mockito.when(transactionProvider.daily(Mockito.any())).thenReturn(Collections.List());
    }

    @Test
    void calculate() {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        subject.calculate(request).blockingGet();

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).balance(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    void daily() {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        subject.daily(request)
                .test()
                .assertComplete();

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).daily(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateUtils.forMonth(2019, 1));
    }

    @Test
    void calculatePartitioned() {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(new BalanceRequest.DateRange(
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 1)));

        var accountMock = Mockito.mock(AccountProvider.class);

        Mockito.when(applicationContext.getBean(AccountProvider.class))
                .thenReturn(accountMock);
        Mockito.when(accountMock.lookup()).thenReturn(Collections.List());

        subject.calculatePartitioned("account", request)
                .test()
                .assertComplete()
                .assertValueCount(1);
    }
}
