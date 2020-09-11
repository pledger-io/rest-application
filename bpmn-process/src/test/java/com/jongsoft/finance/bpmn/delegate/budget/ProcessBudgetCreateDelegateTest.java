package com.jongsoft.finance.bpmn.delegate.budget;

import java.util.Arrays;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.domain.user.events.BudgetCreatedEvent;
import com.jongsoft.finance.domain.user.events.BudgetExpenseCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.BudgetJson;
import com.jongsoft.lang.API;

import io.micronaut.context.event.ApplicationEventPublisher;

class ProcessBudgetCreateDelegateTest {

    private BudgetProvider budgetProvider;
    private DelegateExecution execution;
    private ApplicationEventPublisher eventPublisher;
    private CurrentUserProvider currentUserFacade;

    private ProcessBudgetCreateDelegate subject;

    @BeforeEach
    void setup() throws JsonProcessingException {
        budgetProvider = Mockito.mock(BudgetProvider.class);
        execution = Mockito.mock(DelegateExecution.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        currentUserFacade = Mockito.mock(CurrentUserProvider.class);

        subject = new ProcessBudgetCreateDelegate(currentUserFacade, budgetProvider);

        final BudgetJson budgetJson = new BudgetJson();
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

        Mockito.when(currentUserFacade.currentUser()).thenReturn(UserAccount.builder().roles(API.List(new Role("admin"))).build());
        Mockito.when(execution.getVariableLocalTyped("budget")).thenReturn(value);

        new EventBus(eventPublisher);
    }

    @Test
    void execute_initial() {
        Mockito.when(budgetProvider.first()).thenReturn(API.Option());

        subject.execute(execution);

        Mockito.verify(budgetProvider).first();
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(BudgetCreatedEvent.class));
        Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(Mockito.any(BudgetExpenseCreatedEvent.class));
    }

}
