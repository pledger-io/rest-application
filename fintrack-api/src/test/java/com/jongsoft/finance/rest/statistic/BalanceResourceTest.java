package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.time.LocalDate;

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

        StepVerifier.create(subject.calculate(request))
                .expectNextCount(1)
                .verifyComplete();

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

        StepVerifier.create(subject.daily(request))
                .verifyComplete();

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

        StepVerifier.create(subject.calculatePartitioned("account", request))
                .expectNextCount(1)
                .verifyComplete();
    }
}
