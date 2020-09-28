package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.API;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ContractTransactionResourceTest extends TestSetup {

    private ContractTransactionResource subject;

    @Mock
    private TransactionProvider transactionProvider;
    @Mock
    private SettingProvider settingProvider;
    private FilterFactory filterFactory;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filterFactory = generateFilterMock();

        subject = new ContractTransactionResource(
                filterFactory,
                transactionProvider,
                settingProvider);
    }

    @Test
    void transactions() {
        Mockito.when(transactionProvider.lookup(Mockito.any())).thenReturn(ResultPage.empty());

        subject.transactions(1L, 1);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(transactionProvider).lookup(Mockito.any());
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).contracts(API.List(new EntityRef(1L)));
    }
}