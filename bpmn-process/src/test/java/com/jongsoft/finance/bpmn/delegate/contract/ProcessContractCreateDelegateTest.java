package com.jongsoft.finance.bpmn.delegate.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.serialized.ContractJson;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

class ProcessContractCreateDelegateTest {

    private static final UserAccount USER_ACCOUNT = UserAccount.builder().roles(Collections.List(new Role("admin"))).build();
    private AccountProvider accountProvider;
    private ContractProvider contractProvider;
    private StorageService storageService;

    private ProcessContractCreateDelegate subject;

    private DelegateExecution execution;

    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setup() throws JsonProcessingException {
        accountProvider = Mockito.mock(AccountProvider.class);
        contractProvider = Mockito.mock(ContractProvider.class);
        storageService = Mockito.mock(StorageService.class);
        execution = Mockito.mock(DelegateExecution.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        subject = new ProcessContractCreateDelegate(accountProvider, contractProvider, storageService);

        final ContractJson contractJson = ContractJson.builder()
                .name("Test contract")
                .company("Telfo")
                .description("My personal contract")
                .terminated(true)
                .start(LocalDate.of(2018, 1, 1))
                .end(LocalDate.of(2019, 1, 1))
                .build();

        final String value = ProcessMapper.writeSafe(contractJson);
        Mockito.when(execution.getVariableLocalTyped("contract")).thenReturn(new PrimitiveTypeValueImpl.StringValueImpl(value));

        new EventBus(eventPublisher);
    }

    @Test
    void execute() throws Exception {
        Mockito.when(accountProvider.lookup("Telfo"))
                .thenReturn(Control.Option(Account.builder()
                        .id(1L)
                        .user(USER_ACCOUNT)
                        .build()));

        Contract contract = Contract.builder()
                .id(1L)
                .startDate(LocalDate.of(2018, 1, 1))
                .endDate(LocalDate.of(2019, 1, 1))
                .build();
        Mockito.when(contractProvider.lookup("Test contract"))
                .thenReturn(Control.Option())
                .thenReturn(Control.Option(contract));

        subject.execute(execution);

        Mockito.verify(accountProvider).lookup("Telfo");
        Mockito.verify(contractProvider, Mockito.times(2)).lookup("Test contract");
    }
}
