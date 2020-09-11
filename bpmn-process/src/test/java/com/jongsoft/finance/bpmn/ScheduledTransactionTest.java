package com.jongsoft.finance.bpmn;

import java.time.LocalDate;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import com.jongsoft.finance.StorageService;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.AccountProvider;
import com.jongsoft.finance.domain.transaction.ScheduledTransaction;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionCreationHandler;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.domain.transaction.TransactionScheduleProvider;
import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;
import com.jongsoft.lang.API;

import io.micronaut.core.reflect.ReflectionUtils;

public class ScheduledTransactionTest extends ProcessTestSetup {

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

        Mockito.when(transactionScheduleProvider.lookup()).thenReturn(API.List(
                ScheduledTransaction.builder()
                        .id(1L)
                        .amount(29.99)
                        .description("Monthly TV")
                        .source(Account.builder().id(1L).build())
                        .destination(Account.builder().id(2L).build())
                        .build()));
        Mockito.when(transactionProvider.similar(Mockito.any(), Mockito.any(), Mockito.anyDouble(), Mockito.any()))
                .thenReturn(API.List());
        Mockito.when(transactionRuleProvider.lookup()).thenReturn(API.List());

        Mockito.when(accountProvider.lookup(1L)).thenReturn(API.Option(Account.builder()
                .id(1L)
                .name("Source account")
                .type("checking")
                .build()));
        Mockito.when(accountProvider.lookup(2L)).thenReturn(API.Option(Account.builder()
                .id(2L)
                .name("Cable Company")
                .type("creditor")
                .build()));

        Mockito.when(storageService.store(Mockito.any())).thenAnswer((Answer<String>) invocation -> {
            byte[] original = invocation.getArgument(0);
            String token = UUID.randomUUID().toString();
            Mockito.when(storageService.read(token)).thenReturn(original);
            return token;
        });
    }

    @Test
    void scheduleRun() {
        MutableLong id = new MutableLong(1);
        MutableObject<Transaction> transaction = new MutableObject<>();

        Mockito.when(transactionCreationHandler.handleCreatedEvent(Mockito.any())).thenAnswer((Answer<Long>) invocation -> {
            TransactionCreatedEvent event = invocation.getArgument(0);
            long transactionId = id.getAndAdd(1);
            var field = ReflectionUtils.getRequiredField(Transaction.class, "id");
            field.setAccessible(true);
            field.set(event.getTransaction(), transactionId);
            Mockito.when(transactionProvider.lookup(transactionId)).thenReturn(API.Option(event.getTransaction()));
            transaction.setValue(event.getTransaction());
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
