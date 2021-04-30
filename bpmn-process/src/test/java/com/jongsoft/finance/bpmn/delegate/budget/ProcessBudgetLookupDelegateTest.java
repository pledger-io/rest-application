package com.jongsoft.finance.bpmn.delegate.budget;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.providers.ExpenseProvider;

class ProcessBudgetLookupDelegateTest {

    private ExpenseProvider expenseProvider;
    private DelegateExecution execution;
    private FilterFactory filterFactory;
    private ExpenseProvider.FilterCommand filterCommand;

    private ProcessBudgetLookupDelegate subject;

    @BeforeEach
    void setup() {
        filterFactory = Mockito.mock(FilterFactory.class);
        expenseProvider = Mockito.mock(ExpenseProvider.class);
        execution = Mockito.mock(DelegateExecution.class);
        filterCommand = Mockito.mock(ExpenseProvider.FilterCommand.class, InvocationOnMock::getMock);

        subject = new ProcessBudgetLookupDelegate(filterFactory, expenseProvider);

        Mockito.when(filterFactory.expense()).thenReturn(filterCommand);
    }

    @Test
    void execute() {
        Budget.Expense budget = Budget.Expense.builder().build();

        Mockito.when(execution.getVariableLocal("name")).thenReturn("Group 1");
        Mockito.when(expenseProvider.lookup(Mockito.any(ExpenseProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of(budget));

        subject.execute(execution);

        Mockito.verify(execution).setVariable("budget", budget);
        Mockito.verify(filterCommand).name("Group 1", true);
    }

}
