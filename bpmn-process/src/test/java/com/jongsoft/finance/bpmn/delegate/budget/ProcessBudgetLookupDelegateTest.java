package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.ExpenseProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

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
        Mockito.when(execution.getVariableLocal("name")).thenReturn("Group 1");
        Mockito.when(expenseProvider.lookup(Mockito.any(ExpenseProvider.FilterCommand.class)))
                .thenReturn(ResultPage.of(new EntityRef.NamedEntity(1, "Must have")));

        subject.execute(execution);

        Mockito.verify(execution).setVariable("budget", new EntityRef.NamedEntity(1, "Must have"));
        Mockito.verify(filterCommand).name("Group 1", true);
    }

}
