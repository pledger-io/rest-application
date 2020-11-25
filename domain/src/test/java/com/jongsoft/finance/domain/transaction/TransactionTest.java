package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.transaction.events.*;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    private Transaction transaction;

    private Account from;
    private Account to;

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        from = Account.builder()
                .id(1L)
                .type("checking")
                .build();
        to = Account.builder()
                .id(2L)
                .type("creditor")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .created(new Date(2018, 1, 1))
                .description("Transaction into savings")
                .currency("EUR")
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .amount(-25.5)
                                .account(from)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .amount(25.5)
                                .account(to)
                                .build()
                )).build();
    }

    @Test
    void delete() {
        ArgumentCaptor<TransactionDeletedEvent> captor = ArgumentCaptor.forClass(TransactionDeletedEvent.class);
        transaction.delete();

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTransactionId()).isEqualTo(1L);
    }

    @Test
    void register_alreadyPersisted() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> transaction.register());
        assertThat(thrown.getMessage()).isEqualTo("Cannot register transaction it already exists in the system.");
    }

    @Test
    void register_sameAccount() {
        transaction = Transaction.builder()
                .description("Transaction into savings")
                .currency("EUR")
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .amount(-25.5)
                                .account(from)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .amount(25.5)
                                .account(from)
                                .build()
                )).build();

        transaction.register();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(TransactionCreatedEvent.class));
        assertThat(transaction.getFailureCode()).isEqualTo(FailureCode.FROM_TO_SAME);
    }

    @Test
    void register_amountMismatch() {
        transaction = Transaction.builder()
                .description("Transaction into savings")
                .currency("EUR")
                .transactions(Collections.List(
                        Transaction.Part.builder()
                                .id(1L)
                                .amount(-25.5)
                                .account(from)
                                .build(),
                        Transaction.Part.builder()
                                .id(2L)
                                .amount(26.5)
                                .account(to)
                                .build()
                )).build();

        transaction.register();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(TransactionCreatedEvent.class));
        assertThat(transaction.getFailureCode()).isEqualTo(FailureCode.AMOUNT_NOT_NULL);
    }

    @Test
    void changeAccount() {
        ArgumentCaptor<TransactionAccountChangedEvent> captor = ArgumentCaptor.forClass(TransactionAccountChangedEvent.class);

        Account newAccount = Account.builder()
                .id(3L)
                .type("creditor")
                .build();

        transaction.changeAccount(false, newAccount);

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTransactionPartId()).isEqualTo(2L);
        assertThat(captor.getValue().getAccount().getId()).isEqualTo(3L);
        assertThat(transaction.computeTo().getId()).isEqualTo(3L);
        assertThat(transaction.computeFrom().getId()).isEqualTo(1L);
    }

    @Test
    void changeAccount_duplicate() {
        transaction.changeAccount(true, to);

        assertThat(transaction.getFailureCode()).isEqualTo(FailureCode.FROM_TO_SAME);
    }

    @Test
    void book() {
        ArgumentCaptor<TransactionBookedEvent> captor = ArgumentCaptor.forClass(TransactionBookedEvent.class);

        transaction.book(LocalDate.of(2017, 2, 1), LocalDate.of(2017, 2, 2), LocalDate.of(2017, 2, 12));

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTransactionId()).isEqualTo(1L);
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.of(2017, 2, 1));
        assertThat(captor.getValue().getBookDate()).isEqualTo(LocalDate.of(2017, 2, 2));
        assertThat(captor.getValue().getInterestDate()).isEqualTo(LocalDate.of(2017, 2, 12));
    }

    @Test
    void changeAmount() {
        ArgumentCaptor<TransactionAmountChangedEvent> captor = ArgumentCaptor.forClass(TransactionAmountChangedEvent.class);

        transaction.changeAmount(20.5D, "EUR");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTransactionId()).isEqualTo(1L);
        assertThat(captor.getValue().getAmount()).isEqualTo(20.5D);
        assertThat(captor.getValue().getCurrency()).isEqualTo("EUR");
    }

    @Test
    void describe() {
        var captor = ArgumentCaptor.forClass(TransactionDescribeEvent.class);

        transaction.describe("Updated description");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getTransactionId()).isEqualTo(1L);
        assertThat(captor.getValue().getDescription()).isEqualTo("Updated description");
    }

    @Test
    void tag() {
        ArgumentCaptor<TransactionTaggingEvent> captor = ArgumentCaptor.forClass(TransactionTaggingEvent.class);

        transaction.tag(Collections.List("tag 1", "tag 2"));

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getTags()).isEqualTo(Collections.List("tag 1", "tag 2"));
    }

    @Test
    void linkToCategory() {
        ArgumentCaptor<TransactionRelationEvent> captor = ArgumentCaptor.forClass(TransactionRelationEvent.class);

        transaction.linkToCategory("Test-1");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getRelation()).isEqualTo("Test-1");
        assertThat(captor.getValue().getType()).isEqualTo(TransactionRelationEvent.Type.CATEGORY);
    }

    @Test
    void linkToContract() {
        ArgumentCaptor<TransactionRelationEvent> captor = ArgumentCaptor.forClass(TransactionRelationEvent.class);

        transaction.linkToContract("Sample contract");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getRelation()).isEqualTo("Sample contract");
        assertThat(captor.getValue().getType()).isEqualTo(TransactionRelationEvent.Type.CONTRACT);
    }

    @Test
    void linkToBudget() {
        ArgumentCaptor<TransactionRelationEvent> captor = ArgumentCaptor.forClass(TransactionRelationEvent.class);

        transaction.linkToBudget("Budget 1");
        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getRelation()).isEqualTo("Budget 1");
        assertThat(captor.getValue().getType()).isEqualTo(TransactionRelationEvent.Type.EXPENSE);
    }

    @Test
    void linkToImport() {
        transaction.linkToImport("Test-1");
        assertThat(transaction.getImportSlug()).isEqualTo("Test-1");

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> transaction.linkToImport("Test-2"));
        assertThat(thrown.getMessage()).isEqualTo("Cannot link transaction to an import, it's already linked.");
    }

    @Test
    void computeAmount() {
        assertThat(transaction.computeAmount(from)).isEqualTo(-25.5);
        assertThat(transaction.computeAmount(to)).isEqualTo(25.5);
    }

    @Test
    void computeFrom() {
        assertThat(transaction.computeFrom()).isEqualTo(from);
    }

    @Test
    void computeTo() {
        assertThat(transaction.computeTo()).isEqualTo(to);
    }

    @Test
    void computeCounter() {
        assertThat(transaction.computeCounter(from)).isEqualTo(to);
    }

    @Test
    void isDebit() {
        assertThat(transaction.isDebit(from)).isFalse();
        assertThat(transaction.isDebit(to)).isTrue();
    }

    @Test
    void split() {
        transaction.split(Collections.List(
                new SplitRecord("Split part 1", 12),
                new SplitRecord("Split part 2", 13.5)));

        assertThat(transaction.getTransactions()).hasSize(3);
    }

    @Test
    void split_amountChanged() {
        transaction.split(Collections.List(
                new SplitRecord("Split part 1", 12),
                new SplitRecord("Split part 2", 13)));

        assertThat(transaction.getTransactions()).hasSize(3);
        assertThat(transaction.computeAmount(transaction.computeFrom())).isEqualTo(-25);
    }

}
