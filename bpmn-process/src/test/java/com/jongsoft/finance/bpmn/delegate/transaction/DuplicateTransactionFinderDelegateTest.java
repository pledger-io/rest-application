package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateTransactionFinderDelegateTest {

    private static final Account TO_ACCOUNT = Account.builder().id(1L).type("checking").build();
    private static final Account FROM_ACCOUNT = Account.builder().id(2L).type("debtor").build();
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2019, 01, 01);
    private static final Transaction TRANSACTION = Transaction.builder()
            .id(1L)
            .date(TRANSACTION_DATE)
            .currency("EUR")
            .transactions(Collections.List(
                    Transaction.Part.builder()
                            .amount(2100)
                            .account(TO_ACCOUNT)
                            .build(),
                    Transaction.Part.builder()
                            .amount(-2100)
                            .account(FROM_ACCOUNT)
                            .build()
            ))
            .description("Income Salary May 2018")
            .build();

    private TransactionProvider transactionProvider;
    private DelegateExecution execution;

    private DuplicateTransactionFinderDelegate subject;

    @BeforeEach
    void setup() {
        execution = Mockito.mock(DelegateExecution.class);
        transactionProvider = Mockito.mock(TransactionProvider.class);
        new EventBus(Mockito.mock(ApplicationEventPublisher.class));

        subject = new DuplicateTransactionFinderDelegate(transactionProvider);

        Mockito.when(execution.hasVariableLocal("transactionId")).thenReturn(true);
        Mockito.when(execution.getVariableLocalTyped("transactionId")).thenReturn(new PrimitiveTypeValueImpl.LongValueImpl(1L));
        Mockito.when(transactionProvider.lookup(1L)).thenReturn(Control.Option(TRANSACTION));
    }

    @Test
    void execute() throws Exception {
        Mockito.when(transactionProvider.similar(new EntityRef(2L), new EntityRef(1L), -2100, TRANSACTION_DATE)).thenReturn(
                Collections.List(Transaction.builder().id(2L).build()));

        subject.execute(execution);

        assertThat(TRANSACTION.getFailureCode()).isEqualTo(FailureCode.POSSIBLE_DUPLICATE);
        Mockito.verify(transactionProvider).similar(new EntityRef(2L), new EntityRef(1L), -2100, TRANSACTION_DATE);
    }
}
