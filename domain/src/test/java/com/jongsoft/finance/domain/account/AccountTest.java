package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.domain.account.events.*;
import com.jongsoft.finance.domain.transaction.ScheduleValue;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.domain.transaction.events.TransactionCreatedEvent;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.account.*;
import com.jongsoft.finance.messaging.commands.contract.CreateContractCommand;
import com.jongsoft.finance.schedule.Periodicity;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
        ArgumentCaptor<RenameAccountCommand> changeCaptor = ArgumentCaptor.forClass(RenameAccountCommand.class);

        account.rename("New account name", "Updated account", "USD", "checking");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(account.getName()).isEqualTo("New account name");
        assertThat(account).hasToString("New account name");
        assertThat(account.getDescription()).isEqualTo("Updated account");
        assertThat(account.getCurrency()).isEqualTo("USD");
    }

    @Test
    void registerIcon() {
        ArgumentCaptor<RegisterAccountIconCommand> changeCaptor = ArgumentCaptor.forClass(RegisterAccountIconCommand.class);

        account.registerIcon("file-code");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().fileCode()).isEqualTo("file-code");
        assertThat(changeCaptor.getValue().id()).isEqualTo(account.getId());
    }

    @Test
    void registerSynonym() {
        ArgumentCaptor<RegisterSynonymCommand> changeCaptor = ArgumentCaptor.forClass(RegisterSynonymCommand.class);

        account.registerSynonym("Sample synonym");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(changeCaptor.getValue()).isNotNull();
        assertThat(changeCaptor.getValue().accountId()).isEqualTo(account.getId());
        assertThat(changeCaptor.getValue().synonym()).isEqualTo("Sample synonym");
    }

    @Test
    void rename_unchanged() {
        account.rename("Test account", "Account setup for testing", "EUR", "checking");
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(RenameAccountCommand.class);
    }

    @Test
    void changeAccount_changed() {
        ArgumentCaptor<ChangeAccountCommand> changeCaptor = ArgumentCaptor.forClass(ChangeAccountCommand.class);

        account.changeAccount("NLINBS909392832", null, "909392832");

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(account.getIban()).isEqualTo("NLINBS909392832");
        assertThat(account.getNumber()).isEqualTo("909392832");
        assertThat(account.getBic()).isNull();
    }

    @Test
    void changeAccount_notChanged() {
        account.changeAccount("NLINBS909392833", null, null);

        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(ChangeAccountCommand.class);
        assertThat(account.getIban()).isEqualTo("NLINBS909392833");
        assertThat(account.getNumber()).isNull();
        assertThat(account.getBic()).isNull();
    }

    @Test
    void interest() {
        ArgumentCaptor<ChangeInterestCommand> changeCaptor = ArgumentCaptor.forClass(ChangeInterestCommand.class);

        account.interest(0.02, Periodicity.YEARS);

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());
        assertThat(changeCaptor.getValue().id()).isEqualTo(1L);
        assertThat(changeCaptor.getValue().interest()).isEqualTo(0.02);
        assertThat(changeCaptor.getValue().periodicity()).isEqualTo(Periodicity.YEARS);
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
        ArgumentCaptor<TerminateAccountCommand> changeCaptor = ArgumentCaptor.forClass(TerminateAccountCommand.class);

        account.terminate();

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        assertThat(account.isRemove()).isTrue();
        assertThat(changeCaptor.getValue().id()).isEqualTo(account.getId());
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
        ArgumentCaptor<CreateContractCommand> changeCaptor = ArgumentCaptor.forClass(CreateContractCommand.class);

        account.createContract("Sample contract", "", LocalDate.of(2009, 1, 1), LocalDate.of(2010, 1, 1));

        Mockito.verify(applicationEventPublisher).publishEvent(changeCaptor.capture());

        final CreateContractCommand event = changeCaptor.getValue();
        assertThat(event.companyId()).isEqualTo(account.getId());
        assertThat(event.name()).isEqualTo("Sample contract");
        assertThat(event.start()).isEqualTo(LocalDate.of(2009, 1, 1));
        assertThat(event.end()).isEqualTo(LocalDate.of(2010, 1, 1));
    }

}
