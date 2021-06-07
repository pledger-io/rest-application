package com.jongsoft.finance.rest.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.contract.ContractTransactionResource;
import com.jongsoft.lang.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTransactionResourceTest extends TestSetup {

    private ExpenseTransactionResource subject;

    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private SettingProvider settingProvider;
    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filterFactory = generateFilterMock();

        subject = new ExpenseTransactionResource(
                filterFactory,
                transactionProvider,
                settingProvider);
    }

    @Test
    void transactions() {
        Mockito.when(transactionProvider.lookup(Mockito.any())).thenReturn(ResultPage.empty());

        subject.transactions(1L, 2016, 1, 1);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).expenses(Collections.List(new EntityRef(1L)));
    }
}