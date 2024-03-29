package com.jongsoft.finance.bpmn.delegate.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.bpmn.TestUtilities;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.account.CreateAccountCommand;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.AccountJson;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessAccountCreationDelegateTest {

    @Mock
    private AccountProvider accountProvider;
    @Mock
    private CurrentUserProvider userService;
    @Mock
    private StorageService storageService;
    @Mock
    private DelegateExecution execution;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProcessAccountCreationDelegate subject;

    @BeforeEach
    void setup() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        subject = new ProcessAccountCreationDelegate(userService, accountProvider, storageService, TestUtilities.getProcessMapper());

        final AccountJson accountJson = AccountJson.builder()
                .name("Test account")
                .bic("1234")
                .number("9123")
                .currency("EUR")
                .build();

        StringValue value = new PrimitiveTypeValueImpl.StringValueImpl(TestUtilities.getProcessMapper().writeSafe(accountJson));

        Mockito.when(userService.currentUser()).thenReturn(UserAccount.builder().roles(Collections.List(new Role("admin"))).build());
        Mockito.when(execution.getVariableLocalTyped("account")).thenReturn(value);

        new EventBus(eventPublisher);
    }

    @Test
    void execute_alreadyExists() {
        Mockito.when(accountProvider.lookup("Test account")).thenReturn(Control.Option(Account.builder().build()));

        subject.execute(execution);

        Mockito.verify(accountProvider).lookup("Test account");
    }

    @Test
    void execute() {
        Account account = Account.builder().id(1L).build();
        Mockito.when(accountProvider.lookup("Test account"))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(account));

        subject.execute(execution);

        Mockito.verify(accountProvider, Mockito.times(2)).lookup("Test account");
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateAccountCommand.class));

        assertThat(account.getNumber()).isEqualTo("9123");
        assertThat(account.getBic()).isEqualTo("1234");
        assertThat(account.getName()).isEqualTo("Test account");

    }
}
