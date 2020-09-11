package com.jongsoft.finance.domain.account;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.account.events.AccountChangedEvent;
import com.jongsoft.finance.domain.account.events.AccountInterestEvent;
import com.jongsoft.finance.domain.account.events.AccountRenamedEvent;
import com.jongsoft.finance.domain.account.events.AccountSynonymEvent;
import com.jongsoft.finance.domain.account.events.AccountTerminatedEvent;
import com.jongsoft.finance.domain.account.events.ContractCreatedEvent;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.schedule.Periodicity;

import io.micronaut.context.event.ApplicationEventPublisher;

class AccountTest {

    private Account account;
    private Account account2;

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        account = Account.builder()
                .id(1L)
                .name("Test account")
                .description("Account setup for testing")
                .iban("NLINBS909392833")
                .user(UserAccount.builder().username("demo-user").build())
                .currency("EUR")
                .type("checking")
                .build();
        account2 = Account.builder()
                .id(2L)
                .name("To account")
                .description("Second account")
                .iban("NLINBS90939284323")
                .user(UserAccount.builder().username("demo-user").build())
                .currency("EUR")
                .type("debtor")
                .build();
    }

    @Test
    void isManaged() {
        assertThat(account.isManaged()).isTrue();
        assertThat(account2.isManaged()).isFalse();
    }

    @Test
    void rename_changed() {
        ArgumentCaptor<AccountRenamedEvent> changeCaptor = ArgumentCaptor.forClass(AccountRenamedEvent.class);

        account.rename("New account name", "Updated account", "USD", "checking");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(account.getName()).isEqualTo("New account name");
        assertThat(account).hasToString("New account name");
        assertThat(account.getDescription()).isEqualTo("Updated account");
        assertThat(account.getCurrency()).isEqualTo("USD");
    }

    @Test
    void registerSynonym() {
        ArgumentCaptor<AccountSynonymEvent> changeCaptor = ArgumentCaptor.forClass(AccountSynonymEvent.class);

        account.registerSynonym("Sample synonym");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue()).isNotNull();
        assertThat(changeCaptor.getValue().getAccountId()).isEqualTo(account.getId());
        assertThat(changeCaptor.getValue().getSynonym()).isEqualTo("Sample synonym");
    }

    @Test
    void rename_unchanged() {
        ArgumentCaptor<AccountRenamedEvent> changeCaptor = ArgumentCaptor.forClass(AccountRenamedEvent.class);
        account.rename("Test account", "Account setup for testing", "EUR", "checking");
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(changeCaptor.capture());
    }

    @Test
    void changeAccount_changed() {
        ArgumentCaptor<AccountChangedEvent> changeCaptor = ArgumentCaptor.forClass(AccountChangedEvent.class);

        account.changeAccount("NLINBS909392832", null, "909392832");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(account.getIban()).isEqualTo("NLINBS909392832");
        assertThat(account.getNumber()).isEqualTo("909392832");
        assertThat(account.getBic()).isNull();
    }

    @Test
    void changeAccount_notchanged() {
        ArgumentCaptor<AccountChangedEvent> changeCaptor = ArgumentCaptor.forClass(AccountChangedEvent.class);

        account.changeAccount("NLINBS909392833", null, null);

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(changeCaptor.capture());
        assertThat(account.getIban()).isEqualTo("NLINBS909392833");
        assertThat(account.getNumber()).isNull();
        assertThat(account.getBic()).isNull();
    }

    @Test
    void interest() {
        ArgumentCaptor<AccountInterestEvent> changeCaptor = ArgumentCaptor.forClass(AccountInterestEvent.class);

        account.interest(0.02, Periodicity.YEARS);

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().getAccountId()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().getInterest()).isEqualTo(0.02);
        assertThat(changeCaptor.getValue().getInterestPeriodicity()).isEqualTo(Periodicity.YEARS);
    }

    @Test
    void interest_unlikely() {
        var exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> account.interest(2.02, Periodicity.YEARS));

        assertThat(exception.getMessage()).isEqualTo("Highly improbable interest of more than 200%.");
    }

    @Test
    void terminate() {
        ArgumentCaptor<AccountTerminatedEvent> changeCaptor = ArgumentCaptor.forClass(AccountTerminatedEvent.class);

        account.terminate();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(account.isRemove()).isTrue();
        assertThat(changeCaptor.getValue().getAccount()).isEqualTo(account);
    }

    @Test
    void createTransaction_debit() {
        ArgumentCaptor<TransactionCreatedEvent> changeCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);

        final Transaction transaction = account.createTransaction(account2, 2500, Transaction.Type.DEBIT, t -> {});
        transaction.register();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(transaction.computeType()).isEqualTo(Transaction.Type.DEBIT);
        assertThat(transaction.getTransactions()).hasSize(2);
        assertThat(transaction.getTransactions().get(0).getAccount()).isEqualTo(account2);
        assertThat(transaction.getTransactions().get(0).getAmount()).isEqualTo(-2500.0);
        assertThat(transaction.getTransactions().get(1).getAccount()).isEqualTo(account);
        assertThat(transaction.getTransactions().get(1).getAmount()).isEqualTo(2500.0);
    }

    @Test
    void createTransaction_credit() {
        ArgumentCaptor<TransactionCreatedEvent> changeCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);

        final Transaction transaction = account.createTransaction(account2, 2500, Transaction.Type.CREDIT, t -> {});
        transaction.register();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(transaction.computeType()).isEqualTo(Transaction.Type.CREDIT);
        assertThat(transaction.getTransactions()).hasSize(2);
        assertThat(transaction.getTransactions().get(0).getAccount()).isEqualTo(account);
        assertThat(transaction.getTransactions().get(0).getAmount()).isEqualTo(-2500.0);
        assertThat(transaction.getTransactions().get(1).getAccount()).isEqualTo(account2);
        assertThat(transaction.getTransactions().get(1).getAmount()).isEqualTo(2500.0);
    }

    @Test
    void createSchedule() {
        var schedule = account.createSchedule("schedule", new ScheduleValue(Periodicity.WEEKS, 4), account2, 20.22);

        assertThat(schedule.getName()).isEqualTo("schedule");
        assertThat(schedule.getAmount()).isEqualTo(20.22);
        assertThat(schedule.getDestination()).isEqualTo(account2);
        assertThat(schedule.getSource()).isEqualTo(account);
        assertThat(schedule.getSchedule().interval()).isEqualTo(4);
        assertThat(schedule.getSchedule().periodicity()).isEqualTo(Periodicity.WEEKS);
    }

    @Test
    void createContract() {
        ArgumentCaptor<ContractCreatedEvent> changeCaptor = ArgumentCaptor.forClass(ContractCreatedEvent.class);

        account.createContract("Sample contract", "", LocalDate.of(2009, 1, 1), LocalDate.of(2010, 1, 1));

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        final ContractCreatedEvent event = changeCaptor.getValue();
        assertThat(event.getCompany()).isEqualTo(account);
        assertThat(event.getName()).isEqualTo("Sample contract");
        assertThat(event.getStart()).isEqualTo(LocalDate.of(2009, 1, 1));
        assertThat(event.getEnd()).isEqualTo(LocalDate.of(2010, 1, 1));
    }

}
