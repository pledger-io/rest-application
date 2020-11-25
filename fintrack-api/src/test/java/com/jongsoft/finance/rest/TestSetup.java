package com.jongsoft.finance.rest;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.domain.user.ExpenseProvider;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.lang.Collections;
import org.jboss.aerogear.security.otp.api.Base32;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.util.Currency;

public class TestSetup {

    protected final UserAccount ACTIVE_USER = UserAccount.builder()
            .id(1L)
            .username("test-user")
            .password("1234")
            .theme("dark")
            .primaryCurrency(Currency.getInstance("EUR"))
            .secret(Base32.random())
            .roles(Collections.List(new Role("admin")))
            .build();

    protected FilterFactory generateFilterMock() {
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
