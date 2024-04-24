package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.bpmn.TestUtilities;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.budget.CloseBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.messaging.commands.budget.UpdateExpenseCommand;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.BudgetJson;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;

class ProcessBudgetCreateDelegateTest {

    private BudgetProvider budgetProvider;
    private DelegateExecution execution;
    private ApplicationEventPublisher eventPublisher;

    private ProcessBudgetCreateDelegate subject;

    @BeforeEach
    void setup() {
        budgetProvider = Mockito.mock(BudgetProvider.class);
        execution = Mockito.mock(DelegateExecution.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        var currentUserFacade = Mockito.mock(CurrentUserProvider.class);

        subject = new ProcessBudgetCreateDelegate(budgetProvider, TestUtilities.getProcessMapper());

        final BudgetJson budgetJson = BudgetJson.builder()
                .start(LocalDate.of(2019, 1, 1))
                .expectedIncome(1200)
                .expenses(Arrays.asList(
                        BudgetJson.ExpenseJson.builder()
                                .lowerBound(5)
                                .upperBound(10)
                                .name("Expense 1")
                                .build(),
                        BudgetJson.ExpenseJson.builder()
                                .lowerBound(50)
                                .upperBound(100)
                                .name("Expense 2")
                                .build()))
                .build();

        StringValue value = new PrimitiveTypeValueImpl.StringValueImpl(TestUtilities.getProcessMapper().writeSafe(budgetJson));

        Mockito.when(currentUserFacade.currentUser()).thenReturn(UserAccount.builder().roles(Collections.List(new Role("admin"))).build());
        Mockito.when(execution.getVariableLocalTyped("budget")).thenReturn(value);

        new EventBus(eventPublisher);
    }

    @Test
    void execute_initial() {
        Mockito.when(budgetProvider.lookup(2019, 1))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(Budget.builder()
                        .id(1L)
                        .expectedIncome(1200)
                        .expenses(Collections.List())
                        .build()));

        subject.execute(execution);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
        Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(Mockito.any(CreateExpenseCommand.class));
    }

    @Test
    void execute_indexation() {
        var initial = Mockito.spy(Budget.builder()
                .id(1L)
                .start(LocalDate.of(2018, 1, 1))
                .expectedIncome(1100)
                .build());
        initial.new Expense(1, "Expense 1", 15);

        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(Control.Option(initial));

        subject.execute(execution);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CloseBudgetCommand.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateExpenseCommand.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(UpdateExpenseCommand.class));
    }

}
