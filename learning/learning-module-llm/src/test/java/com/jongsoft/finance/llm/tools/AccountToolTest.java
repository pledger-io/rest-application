package com.jongsoft.finance.llm.tools;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.llm.dto.AccountDTO;
import com.jongsoft.finance.providers.*;
import com.jongsoft.lang.Control;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountToolTest {

    @Test
    void lookup() {
        var mockAccountProvider = mock(AccountProvider.class);
        var subject = new AccountTool(mockAccountProvider, generateFilterMock());

        when(mockAccountProvider.lookup(any(AccountProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of(Account.builder().id(1L).name("My account").type("checking").build()));

        var response = subject.lookup("My account");

        assertThat(response)
                .isNotNull()
                .isEqualTo(new AccountDTO(1L, "My account", "checking"));
    }

    @Test
    void lookupFallback() {
        var mockAccountProvider = mock(AccountProvider.class);
        var subject = new AccountTool(mockAccountProvider, generateFilterMock());

        when(mockAccountProvider.synonymOf("My account"))
                .thenReturn(Control.Option(Account.builder().id(1L).name("My account").type("checking").build()));

        var response = subject.lookup("My account");

        assertThat(response)
                .isNotNull()
                .isEqualTo(new AccountDTO(1L,"My account", "checking"));

        verify(mockAccountProvider).synonymOf("My account");
    }

    FilterFactory generateFilterMock() {
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
        Mockito.when(filterFactory.schedule())
                .thenReturn(Mockito.mock(TransactionScheduleProvider.FilterCommand.class, InvocationOnMock::getMock));
        return filterFactory;
    }
}
