package com.jongsoft.finance.bpmn.delegate.user;

import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.user.CreateUserCommand;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CreateUserDelegateTest {

    private CreateUserDelegate subject;

    @Mock
    private DelegateExecution execution;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new CreateUserDelegate();

        new EventBus(eventPublisher);
    }

    @Test
    void execute() throws Exception {
        Mockito.when(execution.getVariableLocalTyped("username"))
                .thenReturn(new PrimitiveTypeValueImpl.StringValueImpl("test-user"));
        Mockito.when(execution.getVariableLocalTyped("password"))
                .thenReturn(new PrimitiveTypeValueImpl.StringValueImpl("password"));

        subject.execute(execution);

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateUserCommand.class));
    }

}
