package com.jongsoft.finance.rest.contract;

import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.Collections;
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
        Mockito.verify(mockFilter).contracts(Collections.List(new EntityRef(1L)));
    }
}
