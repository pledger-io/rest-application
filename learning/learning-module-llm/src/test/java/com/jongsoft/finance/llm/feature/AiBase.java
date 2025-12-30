package com.jongsoft.finance.llm.feature;

import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.UserIdentifier;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.*;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

@MicronautTest(environments = {"ai", "test"})
class AiBase {

    @Inject
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setupBus() {
        new EventBus(eventPublisher);
    }

    @MockBean
    BudgetProvider budgetProvider() {
        return Mockito.mock(BudgetProvider.class);
    }

    @MockBean
    CategoryProvider categoryProvider() {
        return Mockito.mock(CategoryProvider.class);
    }

    @MockBean
    TagProvider tagProvider() {
        return Mockito.mock(TagProvider.class);
    }

    @MockBean
    TransactionProvider transactionProvider() {
        return Mockito.mock(TransactionProvider.class);
    }

    @MockBean
    ExpenseProvider expenseProvider() {
        return Mockito.mock(ExpenseProvider.class);
    }

    @MockBean
    CurrentUserProvider currentUserProvider() {
        var mock = Mockito.mock(CurrentUserProvider.class);
        Mockito.when(mock.currentUser()).thenReturn(UserAccount.builder().username(new UserIdentifier("test@user")).build());
        return mock;
    }

    @MockBean
    AccountProvider accountProvider() {
        return Mockito.mock(AccountProvider.class);
    }

    @MockBean
    UserProvider userProvider() {
        return Mockito.mock(UserProvider.class);
    }

    @MockBean
    @Replaces
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
