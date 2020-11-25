package com.jongsoft.finance.bpmn.delegate.account;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.reactivex.Maybe;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProcessAccountLookupDelegateTest {

    private AccountProvider accountProvider;
    private ProcessAccountLookupDelegate subject;

    private DelegateExecution execution;

    private AccountProvider.FilterCommand filterCommand;

    @BeforeEach
    void setup() {
        accountProvider = Mockito.mock(AccountProvider.class);
        execution = Mockito.mock(DelegateExecution.class);

        var filterFactory = Mockito.mock(FilterFactory.class);
        filterCommand = Mockito.mock(
                AccountProvider.FilterCommand.class,
                invocation -> invocation.getMock());

        Mockito.when(filterFactory.account()).thenReturn(filterCommand);

        subject = new ProcessAccountLookupDelegate(accountProvider, filterFactory);
    }

    @Test
    void execute_byId() {
        final Account account = Account.builder().id(1L).build();

        Mockito.when(execution.hasVariableLocal("id")).thenReturn(true);
        Mockito.when(execution.getVariableLocal("id")).thenReturn(1L);
        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(account));

        subject.execute(execution);

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.anyString());

        Mockito.verify(execution).setVariableLocal("id", account.getId());
    }

    @Test
    void execute_byName() {
        final Account account = Account.builder().id(2L).build();

        Mockito.when(execution.hasVariableLocal("name")).thenReturn(true);
        Mockito.when(execution.getVariableLocal("name")).thenReturn("Test account");
        Mockito.when(accountProvider.lookup("Test account")).thenReturn(Maybe.just(account));

        subject.execute(execution);

        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.anyLong());
        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(accountProvider).lookup("Test account");

        Mockito.verify(execution).setVariableLocal("id", 2L);
    }

    @Test
    void execute_byIban() {
        final Account account = Account.builder().id(3L).build();

        var resultPage = Mockito.mock(ResultPage.class);

        Mockito.when(resultPage.content()).thenReturn(Collections.List(account));
        Mockito.when(execution.hasVariableLocal("iban")).thenReturn(true);
        Mockito.when(execution.getVariableLocal("iban")).thenReturn("NL123723712");
        Mockito.when(accountProvider.lookup(Mockito.any(AccountProvider.FilterCommand.class)))
                .thenReturn(resultPage);

        subject.execute(execution);

        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.anyLong());
        Mockito.verify(accountProvider).lookup(Mockito.any(AccountProvider.FilterCommand.class));
        Mockito.verify(accountProvider, Mockito.never()).lookup(Mockito.anyString());

        Mockito.verify(execution).setVariableLocal("id", 3L);
        Mockito.verify(filterCommand).iban("NL123723712", true);
    }

}
