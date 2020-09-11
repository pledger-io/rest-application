package com.jongsoft.finance.bpmn.delegate.account;

import static org.assertj.core.api.Assertions.*;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.account.events.AccountCreatedEvent;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.finance.serialized.AccountJson;
import com.jongsoft.lang.API;

import io.micronaut.context.event.ApplicationEventPublisher;

class ProcessAccountCreationDelegateTest {

    private AccountProvider accountProvider;
    private CurrentUserProvider userService;
    private ProcessAccountCreationDelegate subject;

    private DelegateExecution execution;

    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() throws JsonProcessingException {
        accountProvider = Mockito.mock(AccountProvider.class);
        execution = Mockito.mock(DelegateExecution.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        userService = Mockito.mock(CurrentUserProvider.class);
        subject = new ProcessAccountCreationDelegate(userService, accountProvider);

        final AccountJson accountJson = new AccountJson();
        accountJson.setName("Test account");
        accountJson.setBic("1234");
        accountJson.setNumber("9123");
        accountJson.setCurrency("EUR");

        StringValue value = new PrimitiveTypeValueImpl.StringValueImpl(ProcessMapper.INSTANCE.writeValueAsString(accountJson));

        Mockito.when(userService.currentUser()).thenReturn(UserAccount.builder().roles(API.List(new Role("admin"))).build());
        Mockito.when(execution.getVariableLocalTyped("account")).thenReturn(value);

        new EventBus(eventPublisher);
    }

    @Test
    void execute_alreadyExists() {
        Mockito.when(accountProvider.lookup("Test account")).thenReturn(API.Option(Account.builder().build()));

        subject.execute(execution);

        Mockito.verify(accountProvider).lookup("Test account");
    }

    @Test
    void execute() {
        Account account = Account.builder().build();
        Mockito.when(accountProvider.lookup("Test account"))
                .thenReturn(API.Option())
                .thenReturn(API.Option(account));

        subject.execute(execution);

        Mockito.verify(accountProvider, Mockito.times(2)).lookup("Test account");
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(AccountCreatedEvent.class));

        assertThat(account.getNumber()).isEqualTo("9123");
        assertThat(account.getBic()).isEqualTo("1234");
        assertThat(account.getName()).isEqualTo("Test account");

    }
}
