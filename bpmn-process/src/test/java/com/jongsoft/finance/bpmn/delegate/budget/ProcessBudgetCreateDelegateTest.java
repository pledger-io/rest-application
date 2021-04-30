package com.jongsoft.finance.bpmn.delegate.budget;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Budget;
import com.jongsoft.finance.messaging.commands.budget.CreateBudgetCommand;
import com.jongsoft.finance.messaging.commands.budget.CreateExpenseCommand;
import com.jongsoft.finance.providers.BudgetProvider;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.BudgetJson;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Single;
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
    private CurrentUserProvider currentUserFacade;

    private ProcessBudgetCreateDelegate subject;

    @BeforeEach
    void setup() {
        budgetProvider = Mockito.mock(BudgetProvider.class);
        execution = Mockito.mock(DelegateExecution.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        currentUserFacade = Mockito.mock(CurrentUserProvider.class);

        subject = new ProcessBudgetCreateDelegate(currentUserFacade, budgetProvider);

        final BudgetJson budgetJson = new BudgetJson();
        budgetJson.setStart(LocalDate.of(2019, 1, 1));
        budgetJson.setExpectedIncome(1200);
        budgetJson.setExpenses(Arrays.asList(
                BudgetJson.ExpenseJson.builder()
                        .lowerBound(5)
                        .upperBound(10)
                        .name("Expense 1")
                        .build(),
                BudgetJson.ExpenseJson.builder()
                        .lowerBound(50)
                        .upperBound(100)
                        .name("Expense 2")
                        .build()));

        StringValue value = new PrimitiveTypeValueImpl.StringValueImpl(ProcessMapper.writeSafe(budgetJson));

        Mockito.when(currentUserFacade.currentUser()).thenReturn(UserAccount.builder().roles(Collections.List(new Role("admin"))).build());
        Mockito.when(execution.getVariableLocalTyped("budget")).thenReturn(value);

        new EventBus(eventPublisher);
    }

    @Test
    void execute_initial() {
        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(Single.error(StatusException.notFound("")));

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
                .expenses(Collections.List(
                        Budget.Expense.builder()
                                .id(1L)
                                .name("Expense 1")
                                .lowerBound(10)
                                .upperBound(20)
                                .build()
                ))
                .build());

        Mockito.when(budgetProvider.lookup(2019, 1)).thenReturn(Single.just(initial));

        subject.execute(execution);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateBudgetCommand.class));
        Mockito.verify(initial).indexBudget(LocalDate.of(2019, 1, 1), 1200);
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateExpenseCommand.class));
    }

}
