package com.jongsoft.finance.bpmn;

import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.domain.transaction.*;
import com.jongsoft.finance.providers.TransactionProvider;
import com.jongsoft.finance.providers.TransactionRuleProvider;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.core.reflect.ReflectionUtils;
import io.reactivex.Single;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

public class ScheduledTransactionIT extends ProcessTestSetup {

    @Inject
    private StorageService storageService;

    @Inject
    private TransactionProvider transactionProvider;

    @Inject
    private TransactionScheduleProvider transactionScheduleProvider;

    @Inject
    private TransactionCreationHandler transactionCreationHandler;

    @Inject
    private TransactionRuleProvider transactionRuleProvider;

    @Inject
    private AccountProvider accountProvider;

    @Inject
    private ProcessEngine processEngine;

    @BeforeEach
    void setup() {
        Mockito.reset(transactionProvider, storageService, accountProvider, transactionCreationHandler, transactionScheduleProvider, transactionRuleProvider);

        Mockito.when(transactionScheduleProvider.lookup()).thenReturn(Collections.List(
                ScheduledTransaction.builder()
                        .id(1L)
                        .amount(29.99)
                        .description("Monthly TV")
                        .source(Account.builder().id(1L).build())
                        .destination(Account.builder().id(2L).build())
                        .build()));
        Mockito.when(transactionProvider.similar(Mockito.any(), Mockito.any(), Mockito.anyDouble(), Mockito.any()))
                .thenReturn(Collections.List());
        Mockito.when(transactionRuleProvider.lookup()).thenReturn(Collections.List());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(Control.Option(Account.builder()
                .id(1L)
                .name("Source account")
                .type("checking")
                .build()));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(Control.Option(Account.builder()
                .id(2L)
                .name("Cable Company")
                .type("creditor")
                .build()));

        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(Single.just(original));
            return token;
        });
    }

    @Test
    void scheduleRun() {
        MutableLong id = new MutableLong(1);
        MutableObject<Transaction> transaction = new MutableObject<>();

        Mockito.when(transactionCreationHandler.handleCreatedEvent(Mockito.any())).thenAnswer((Answer<Long>) invocation -> {
            CreateTransactionCommand event = invocation.getArgument(0);
            long transactionId = id.getAndAdd(1);
            var field = ReflectionUtils.getRequiredField(Transaction.class, "id");
            field.setAccessible(true);
            field.set(event.transaction(), transactionId);
            Mockito.when(transactionProvider.lookup(transactionId)).thenReturn(Control.Option(event.transaction()));
            transaction.setValue(event.transaction());
            return transactionId;
        });

        var process = processEngine.getRuntimeService()
                .createProcessInstanceByKey("ScheduledTransaction")
                .setVariable("id", 1L)
                .setVariable("scheduled", "2019-01-01")
                .execute();

        Mockito.verify(accountProvider).lookup(1L);
        Mockito.verify(accountProvider).lookup(2L);

        Assertions.assertThat(transaction.getValue().computeTo().getId()).isEqualTo(2L);
        Assertions.assertThat(transaction.getValue().computeFrom().getId()).isEqualTo(1L);
        Assertions.assertThat(transaction.getValue().computeAmount(transaction.getValue().computeTo())).isEqualTo(29.99);
        Assertions.assertThat(transaction.getValue().getDescription()).isEqualTo("Monthly TV");
        Assertions.assertThat(transaction.getValue().getDate()).isEqualTo(LocalDate.of(2019, 1, 1));
    }
}
