package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.DateUtils;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.lang.Collections;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.time.LocalDate;

class ProcessBudgetAnalysisDelegateTest {

    private TransactionProvider transactionProvider;
    private DelegateExecution execution;

    private ProcessBudgetAnalysisDelegate subject;

    private TransactionProvider.FilterCommand filterCommand;

    @BeforeEach
    void setup() {
        execution = Mockito.mock(DelegateExecution.class);
        transactionProvider = Mockito.mock(TransactionProvider.class);
        filterCommand = Mockito.mock(TransactionProvider.FilterCommand.class, InvocationOnMock::getMock);
        var applicationSettings = Mockito.mock(SettingProvider.class);
        var filterFactory = Mockito.mock(FilterFactory.class);

        subject = new ProcessBudgetAnalysisDelegate(transactionProvider, filterFactory, applicationSettings);

        Mockito.when(filterFactory.transaction()).thenReturn(filterCommand);
        Mockito.when(applicationSettings.getBudgetAnalysisMonths()).thenReturn(3);
        Mockito.when(applicationSettings.getMaximumBudgetDeviation()).thenReturn(0.25);
        Mockito.when(execution.getVariable("user")).thenReturn(UserAccount.builder()
                .roles(Collections.List(new Role("admin")))
                .build());

        Mockito.when(execution.getVariableLocal("date")).thenReturn(LocalDate.of(2019, 1, 23));
//        Mockito.when(execution.getVariableLocal("expense")).thenReturn(Budget.Expense.builder()
//                .lowerBound(100)
//                .upperBound(110)
//                .id(1L)
//                .name("test expense")
//                .build());
    }

    @Test
    @Disabled
    void execute() {
        Account account = Account.builder().build();
        Transaction mockTransaction1 = Mockito.mock(Transaction.class);
        Transaction mockTransaction2 = Mockito.mock(Transaction.class);

        Mockito.when(mockTransaction1.computeTo()).thenReturn(account);
        Mockito.when(mockTransaction2.computeTo()).thenReturn(account);
        Mockito.when(mockTransaction1.computeAmount(account)).thenReturn(60.0);
        Mockito.when(mockTransaction2.computeAmount(account)).thenReturn(80.0);

        Mockito.when(transactionProvider.lookup(Mockito.any(TransactionProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of(mockTransaction1, mockTransaction2));

        subject.execute(execution);

        Mockito.verify(transactionProvider, Mockito.times(3)).lookup(Mockito.any(TransactionProvider.FilterCommand.class));
        Mockito.verify(filterCommand).range(DateUtils.forMonth(2018, 12));
        Mockito.verify(filterCommand).range(DateUtils.forMonth(2018, 11));
        Mockito.verify(filterCommand).range(DateUtils.forMonth(2018, 10));

        Mockito.verify(execution).setVariableLocal("deviates", true);
        Mockito.verify(execution).setVariableLocal("deviation", -35.0d);
    }

}
