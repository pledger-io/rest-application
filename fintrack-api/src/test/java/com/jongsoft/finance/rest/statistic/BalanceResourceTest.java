package com.jongsoft.finance.rest.statistic;

import com.jongsoft.finance.core.date.DateRangeOld;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Control;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BalanceResourceTest extends TestSetup {

    private BalanceResource subject;

    private FilterFactory filterFactory;
    private TransactionProvider transactionProvider;

    @BeforeEach
    void setup() {
        filterFactory = generateFilterMock();
        transactionProvider = Mockito.mock(TransactionProvider.class);
        subject = new BalanceResource(filterFactory, transactionProvider);

        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(Control.Option());
    }

    @Test
    void calculate() {
        var request = new BalanceRequest();
        request.setOnlyIncome(false);
        request.setDateRange(DateRangeOld.forMonth(2019, 1));

        subject.calculate(request).blockingGet();

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).balance(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).range(DateRangeOld.forMonth(2019, 1));
    }

}
